package jk_5.nailed.server;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import jk_5.eventbus.EventBus;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.Platform;
import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent;
import jk_5.nailed.api.event.player.PlayerJoinServerEvent;
import jk_5.nailed.api.event.player.PlayerLeaveServerEvent;
import jk_5.nailed.api.map.GameTypeRegistry;
import jk_5.nailed.api.map.MapLoader;
import jk_5.nailed.api.mappack.MappackRegistry;
import jk_5.nailed.api.messaging.Messenger;
import jk_5.nailed.api.messaging.StandardMessenger;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.plugin.PluginManager;
import jk_5.nailed.api.scheduler.Scheduler;
import jk_5.nailed.api.util.PlayerSelector;
import jk_5.nailed.api.world.DefaultWorldProviders;
import jk_5.nailed.api.world.World;
import jk_5.nailed.api.world.WorldContext;
import jk_5.nailed.api.world.WorldProvider;
import jk_5.nailed.server.command.NailedCommandManager;
import jk_5.nailed.server.config.Settings;
import jk_5.nailed.server.map.NailedMapLoader;
import jk_5.nailed.server.map.game.NailedGameTypeRegistry;
import jk_5.nailed.server.mappack.NailedMappackRegistry;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.plugin.NailedPluginManager$;
import jk_5.nailed.server.scheduler.NailedScheduler$;
import jk_5.nailed.server.teamspeak.TeamspeakManager;
import jk_5.nailed.server.teleport.MapInventoryListener$;
import jk_5.nailed.server.tileentity.OldStatEmitterConverter;
import jk_5.nailed.server.tileentity.TileEntityStatEmitter;
import jk_5.nailed.server.tweaker.NailedTweaker;
import jk_5.nailed.server.utils.InvSeeTicker;
import jk_5.nailed.server.utils.NailedPlayerSelector$;
import jk_5.nailed.server.world.*;
import jk_5.nailed.server.worlditems.WorldItemEventHandler$;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class NailedPlatform implements Platform {

    private static final NailedPlatform INSTANCE = new NailedPlatform();
    private static final String apiVersion = Platform.class.getPackage().getImplementationVersion();
    private static final String implementationVersion = NailedPlatform.class.getPackage().getImplementationVersion();
    private static final String implementationName = "Nailed";
    private static final File runtimeDirectory = NailedTweaker.gameDir;
    private static final Logger logger = LogManager.getLogger();
    private static final Config config = Settings.load();
    private static final EventBus globalEventBus = new EventBus();
    private static final File pluginsDir = new File(NailedTweaker.gameDir, "plugins");
    public static final Gson gson = new Gson();
    private final StandardMessenger messenger = new StandardMessenger(this);

    //Player registry stuff
    private final List<NailedPlayer> players = new ArrayList<NailedPlayer>();
    private NailedPlayer[] onlinePlayers = new NailedPlayer[0];
    private Collection<Player> onlinePlayersCollection = Collections.emptyList();
    private final HashMap<UUID, NailedPlayer> playersById = new HashMap<UUID, NailedPlayer>();
    private final HashMap<String, NailedPlayer> playersByName = new HashMap<String, NailedPlayer>();

    //Dimension registry stuff
    private boolean defaultsRegistered = false;
    private Hashtable<Integer, WorldProvider> customProviders = new Hashtable<Integer, WorldProvider>();
    private Hashtable<Integer, WorldServer> vanillaWorlds = new Hashtable<Integer, WorldServer>();
    private Hashtable<Integer, NailedWorld> worlds = new Hashtable<Integer, NailedWorld>();
    private Hashtable<Integer, WorldContext> worldContext = new Hashtable<Integer, WorldContext>();
    private ArrayList<Integer> dimensions = new ArrayList<Integer>();
    private ArrayList<Integer> unloadQueue = new ArrayList<Integer>();
    private BitSet dimensionMap = new BitSet(java.lang.Long.SIZE << 4);

    private int[] vanillaWorldIdArray = new int[0];

    public NailedPlatform() {
        //Dimension registry
        if(!defaultsRegistered){
            this.dimensionMap.set(1);
            defaultsRegistered = true;
        }

        //Normal
        logger.info("PLATFORM LOADED BY " + this.getClass().getClassLoader());
        if(this.getClass().getClassLoader() == Launch.classLoader){
            globalEventBus.register(this);
            globalEventBus.register(NailedScheduler$.MODULE$);
            globalEventBus.register(NailedMapLoader.instance());
            globalEventBus.register(BossBar$.MODULE$);
            globalEventBus.register(WorldItemEventHandler$.MODULE$);
            globalEventBus.register(new InvSeeTicker());
            globalEventBus.register(MapInventoryListener$.MODULE$);
        }else{
            logger.info("------------------");
            logger.info("WRONG CLASSLOADER!");
            logger.info("------------------");
            Thread.dumpStack();
        }
    }

    public void preLoad(DedicatedServer server){
        CommandBase.setAdminCommander(null); //Don't spam my log with stupid messages

        TileEntity.addMapping(TileEntityStatEmitter.class, "Nailed:StatEmitter");
        TileEntity.addMapping(OldStatEmitterConverter.class, "nailed.stat");

        pluginsDir.mkdir();
        NailedPluginManager$.MODULE$.loadPlugins(pluginsDir);

        NailedEventFactory.fireEvent(new RegisterMappacksEvent(NailedMappackRegistry.instance(), NailedMapLoader.instance()));
        NailedMapLoader.instance().checkLobbyMappack();
        NailedCommandManager.registerPluginCommands();
    }

    public void load(){
        NailedPluginManager$.MODULE$.enablePlugins();
        TeamspeakManager.start();
    }

    @Nonnull
    @Override
    public String getAPIVersion() {
        return apiVersion;
    }

    @Nonnull
    @Override
    public String getImplementationVersion() {
        return implementationVersion;
    }

    @Nonnull
    @Override
    public String getImplementationName() {
        return implementationName;
    }

    @Nonnull
    @Override
    public PluginManager getPluginManager() {
        return NailedPluginManager$.MODULE$;
    }

    @Nonnull
    @Override
    public File getRuntimeDirectory() {
        return runtimeDirectory;
    }

    @Nonnull
    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Nonnull
    @Override
    public GameTypeRegistry getGameTypeRegistry() {
        return NailedGameTypeRegistry.instance();
    }

    @Nullable
    @Override
    public Player getPlayer(UUID uuid) {
        return this.playersById.get(uuid);
    }

    @Nullable
    @Override
    public Player getPlayerByName(String name) {
        return this.playersByName.get(name);
    }

    @Nonnull
    @Override
    public Collection<Player> getOnlinePlayers() {
        return this.onlinePlayersCollection;
    }

    public Player getPlayerFromEntity(EntityPlayerMP entity){
        return this.getPlayer(entity.getGameProfile().getId());
    }

    public NailedPlayer getOrCreatePlayer(EntityPlayerMP entity){
        if(this.getPlayerFromEntity(entity) != null){
            NailedPlayer player = new NailedPlayer(entity.getGameProfile().getId(), entity.getGameProfile().getName());
            this.players.add(player);
            this.playersById.put(entity.getGameProfile().getId(), player);
            this.playersByName.put(entity.getGameProfile().getName(), player);
            return player;
        }else{
            return (NailedPlayer) this.getPlayerFromEntity(entity);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinServerEvent event){
        List<NailedPlayer> b = new ArrayList<NailedPlayer>();
        b.addAll(Arrays.asList(this.onlinePlayers));
        b.add((NailedPlayer) event.getPlayer());
        this.onlinePlayers = b.toArray(new NailedPlayer[b.size()]);
        this.onlinePlayersCollection = ImmutableSet.<Player>copyOf(this.onlinePlayers);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveServerEvent event){
        List<NailedPlayer> b = new ArrayList<NailedPlayer>();
        for(Player p : this.onlinePlayersCollection){
            if(p != event.getPlayer()){
                b.add((NailedPlayer) p);
            }
        }
        this.onlinePlayers = b.toArray(new NailedPlayer[b.size()]);
        this.onlinePlayersCollection = ImmutableSet.<Player>copyOf(this.onlinePlayers);
    }

    @Override
    public void broadcastMessage(BaseComponent... message) {
        TextComponent msg = new TextComponent(message);
        logger.info(msg.toPlainText()); //TODO: format this before we print it out
        MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(new S02PacketChat(msg));
    }

    @Nonnull
    @Override
    public DefaultWorldProviders getDefaultWorldProviders() {
        return NailedDefaultWorldProviders.instance();
    }

    @Nonnull
    @Override
    public World createNewWorld(WorldProvider provider, WorldContext ctx) {
        int id = NailedDimensionManager.getNextFreeDimensionId();
        NailedDimensionManager.registerDimension(id, provider);
        NailedDimensionManager.initWorld(id, ctx);
        return NailedDimensionManager.getWorld(id);
    }

    @Nonnull
    @Override
    public Scheduler getScheduler() {
        return NailedScheduler$.MODULE$;
    }

    @Nonnull
    @Override
    public PlayerSelector getPlayerSelector() {
        return NailedPlayerSelector$.MODULE$;
    }

    @Nonnull
    @Override
    public MappackRegistry getMappackRegistry() {
        return NailedMappackRegistry.instance();
    }

    @Nonnull
    @Override
    public CommandSender getConsoleCommandSender() {
        return NailedEventFactory.serverCommandSender();
    }

    @Nonnull
    @Override
    public MapLoader getMapLoader() {
        return NailedMapLoader.instance();
    }

    //Dimension Registry
    public void registerDimension(int id, WorldProvider provider){
        if(dimensions.contains(id)){
            throw new IllegalArgumentException(String.format("Failed to register dimension for id %d, One is already registered", id));
        }
        customProviders.put(id, provider);
        dimensions.add(id);
        if(id >= 0) dimensionMap.set(id);
    }

    public void unregisterDimension(int id){
        if(!dimensions.contains(id)){
            throw new IllegalArgumentException(String.format("Failed to unregister dimension for id %d; No provider registered", id));
        }
        dimensions.remove(id);
    }

    public boolean isDimensionRegistered(int dim){
        return dimensions.contains(dim);
    }

    public int[] getAllDimensionIds(){
        return this.vanillaWorldIdArray;
    }

    public void setWorld(int id, WorldServer world){
        WorldContext context = this.worldContext.get(id);
        if(world != null){
            NailedWorld nworld = new NailedWorld(world, context);
            this.vanillaWorlds.put(id, world);
            this.vanillaWorldIdArray = ArrayUtils.toPrimitive(this.vanillaWorlds.keySet().<Integer>toArray(new Integer[this.vanillaWorlds.size()]));
            this.worlds.put(id, nworld);
            MinecraftServer.getServer().worldTickTimes.put(id, new long[100]);
            logger.info("Loading dimension {0} ({1}) ({2})", id, world.getWorldInfo().getWorldName(), nworld.toString());
        }else{
            this.vanillaWorlds.remove(id);
            this.vanillaWorldIdArray = ArrayUtils.toPrimitive(this.vanillaWorlds.keySet().<Integer>toArray(new Integer[this.vanillaWorlds.size()]));
            this.worlds.remove(id);
            MinecraftServer.getServer().worldTickTimes.remove(id);
            logger.info("Unloading dimension " + id);
        }
        List<WorldServer> builder = new ArrayList<WorldServer>();
        if(this.vanillaWorlds.get(0) != null) builder.add(vanillaWorlds.get(0));
        for(Map.Entry<Integer, WorldServer> e : this.vanillaWorlds.entrySet()){
            int dim = e.getKey();
            if(dim < -1 || dim > 1) builder.add(e.getValue());
        }
        MinecraftServer.getServer().worldServers = builder.toArray(new WorldServer[builder.size()]);
    }

    public void initWorld(int dimension, WorldContext ctx){
        if(!this.dimensions.contains(dimension) && !this.customProviders.containsKey(dimension)){
            throw new IllegalArgumentException(String.format("Provider type for dimension %d does not exist!", dimension));
        }
        MinecraftServer mcserver = MinecraftServer.getServer();
        String name = ctx.getName() + "/" + ctx.getSubName();
        ISaveHandler saveHandler = mcserver.getActiveAnvilConverter().getSaveLoader(name, true);
        WorldInfo worldInfo = saveHandler.loadWorldInfo(); //Attempt to load level.dat

        WorldSettings worldSettings;
        if(worldInfo == null){ //If the level.dat does not exist, create a new one
            //TODO: populate this from the mappack that may or may not exist
            //Arguments: seed, gameType, enable structures, hardcore mode, worldType
            worldSettings = new WorldSettings(0, WorldSettings.GameType.ADVENTURE, false, false, WorldType.DEFAULT);
            worldSettings.setWorldName(""); //Generator settings (for flat)
            worldInfo = new WorldInfo(worldSettings, name);
        }else{
            worldSettings = new WorldSettings(worldInfo);
        }

        worldContext.put(dimension, ctx);
        WorldServer world = new WorldServer(mcserver, saveHandler, worldInfo, dimension, mcserver.theProfiler);
        world.init();
        world.addWorldAccess(new WorldManager(mcserver, world));
        NailedEventFactory.fireWorldLoad(world);
        world.getWorldInfo().setGameType(mcserver.getGameType());
    }

    public WorldServer getVanillaWorld(int dimension){
        return this.vanillaWorlds.get(dimension);
    }

    public World getWorld(int dimension){
        return this.worlds.get(dimension);
    }

    public WorldServer[] getVanillaWorlds(){
        return this.vanillaWorlds.values().toArray(new WorldServer[this.vanillaWorlds.size()]);
    }

    public World[] getWorlds(){
        return this.worlds.values().toArray(new World[this.worlds.size()]);
    }

    public net.minecraft.world.WorldProvider createProviderFor(int dim){
        if(this.customProviders.containsKey(dim)){
            DelegatingWorldProvider d = new DelegatingWorldProvider(this.customProviders.get(dim));
            d.setDimension(dim);
            return d;
        }else{
            throw new RuntimeException(String.format("No WorldProvider bound for dimension %d", dim));
        }
    }

    public void unloadWorld(int id){
        this.unloadQueue.add(id);
    }

    public void unloadWorlds(Hashtable<Integer, long[]> times){
        for(Integer id : this.unloadQueue){
            WorldServer w = this.vanillaWorlds.get(id);
            try{
                if(w != null){
                    w.saveAllChunks(true, null);
                }else{
                    logger.warn("Unexpected world unload. World " + id + " is already unloaded! Skipping it");
                }
            }catch(MinecraftException e){
                logger.warn("Error while unloading world " + id, e);
            }finally{
                if(w != null){
                    NailedEventFactory.fireWorldUnload(w);
                    w.flush();
                    this.setWorld(id, null);
                }
            }
        }
        this.unloadQueue.clear();
    }

    public int getNextFreeDimensionId(){
        int next = 0;
        while(true){
            next = this.dimensionMap.nextClearBit(next);
            if(dimensions.contains(next)){
                dimensionMap.set(next);
            }else{
                return next;
            }
        }
    }

    public static NailedPlatform instance(){
        return INSTANCE;
    }

    public Config getConfig(){
        return config;
    }

    public Gson getGson(){
        return gson;
    }

    public EventBus getEventBus(){
        return globalEventBus;
    }
}
