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
import jk_5.nailed.server.plugin.NailedPluginManager;
import jk_5.nailed.server.scheduler.NailedScheduler;
import jk_5.nailed.server.teamspeak.TeamspeakManager;
import jk_5.nailed.server.teleport.MapInventoryListener;
import jk_5.nailed.server.tileentity.OldStatEmitterConverter;
import jk_5.nailed.server.tileentity.TileEntityStatEmitter;
import jk_5.nailed.server.tweaker.NailedTweaker;
import jk_5.nailed.server.utils.InvSeeTicker;
import jk_5.nailed.server.utils.NailedPlayerSelector;
import jk_5.nailed.server.world.BossBar;
import jk_5.nailed.server.world.NailedDefaultWorldProviders;
import jk_5.nailed.server.world.NailedDimensionManager;
import jk_5.nailed.server.worlditems.WorldItemEventHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class NailedPlatform implements Platform {

    private static final String apiVersion = Platform.class.getPackage().getImplementationVersion();
    private static final String implementationVersion = NailedPlatform.class.getPackage().getImplementationVersion();
    private static final String implementationName = "Nailed";
    private static final File runtimeDirectory = NailedTweaker.gameDir;
    private static final Logger logger = LogManager.getLogger();
    private static final Config config = Settings.load();
    private static final EventBus globalEventBus = new EventBus();
    private static final File pluginsDir = new File(NailedTweaker.gameDir, "plugins");
    public static final Gson gson = new Gson();
    private static final NailedPlatform INSTANCE = new NailedPlatform();
    private final StandardMessenger messenger = new StandardMessenger(this);

    //Player registry stuff
    private final List<NailedPlayer> players = new ArrayList<NailedPlayer>();
    private NailedPlayer[] onlinePlayers = new NailedPlayer[0];
    private Collection<Player> onlinePlayersCollection = Collections.emptyList();
    private final HashMap<UUID, NailedPlayer> playersById = new HashMap<UUID, NailedPlayer>();
    private final HashMap<String, NailedPlayer> playersByName = new HashMap<String, NailedPlayer>();

    public NailedPlatform() {
        logger.info("PLATFORM LOADED BY " + this.getClass().getClassLoader());
        if(this.getClass().getClassLoader() == Launch.classLoader){
            globalEventBus.register(this);
            globalEventBus.register(NailedScheduler.instance());
            globalEventBus.register(NailedMapLoader.instance());
            globalEventBus.register(BossBar.instance());
            globalEventBus.register(WorldItemEventHandler.instance());
            globalEventBus.register(new InvSeeTicker());
            globalEventBus.register(new MapInventoryListener());
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
        NailedPluginManager.instance().loadPlugins(pluginsDir);

        NailedEventFactory.fireEvent(new RegisterMappacksEvent(NailedMappackRegistry.instance(), NailedMapLoader.instance()));
        NailedMapLoader.instance().checkLobbyMappack();
        NailedCommandManager.registerPluginCommands();
    }

    public void load(){
        NailedPluginManager.instance().enablePlugins();
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
        return NailedPluginManager.instance();
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
        if(this.getPlayerFromEntity(entity) == null){
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
        int id = NailedDimensionManager.instance().getNextFreeDimensionId();
        NailedDimensionManager.instance().registerDimension(id, provider);
        NailedDimensionManager.instance().initWorld(id, ctx);
        return NailedDimensionManager.instance().getWorld(id);
    }

    @Nonnull
    @Override
    public Scheduler getScheduler() {
        return NailedScheduler.instance();
    }

    @Nonnull
    @Override
    public PlayerSelector getPlayerSelector() {
        return NailedPlayerSelector.instance();
    }

    @Nonnull
    @Override
    public MappackRegistry getMappackRegistry() {
        return NailedMappackRegistry.instance();
    }

    @Nonnull
    @Override
    public CommandSender getConsoleCommandSender() {
        return NailedEventFactory.serverCommandSender;
    }

    @Nonnull
    @Override
    public MapLoader getMapLoader() {
        return NailedMapLoader.instance();
    }

    @Nullable
    @Override
    public World getWorld(int dimensionId) {
        return NailedDimensionManager.instance().getWorld(dimensionId);
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
