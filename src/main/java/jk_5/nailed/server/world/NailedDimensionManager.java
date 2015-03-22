package jk_5.nailed.server.world;

import jk_5.nailed.api.world.WorldContext;
import jk_5.nailed.api.world.WorldProvider;
import jk_5.nailed.server.NailedEventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class NailedDimensionManager {

    private static final NailedDimensionManager INSTANCE = new NailedDimensionManager();
    private static final Logger logger = LogManager.getLogger();

    private final Hashtable<Integer, WorldProvider> customProviders = new Hashtable<Integer, WorldProvider>();
    private final Hashtable<Integer, WorldServer> vanillaWorlds = new Hashtable<Integer, WorldServer>();
    private final Hashtable<Integer, NailedWorld> worlds = new Hashtable<Integer, NailedWorld>();
    private final Hashtable<Integer, WorldContext> worldContext = new Hashtable<Integer, WorldContext>();
    private final List<Integer> dimensions = new ArrayList<Integer>();
    private final List<Integer> unloadQueue = new ArrayList<Integer>();
    private final BitSet dimensionMap = new BitSet(java.lang.Long.SIZE << 4);

    private int[] vanillaWorldIdArray = new int[0];

    private NailedDimensionManager() {
        this.dimensionMap.set(1);
    }

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
        return this.dimensions.contains(dim);
    }

    public int[] getAllDimensionIds(){
        return this.vanillaWorldIdArray;
    }

    public void setWorld(int id, WorldServer world){
        WorldContext context = this.worldContext.get(id);
        if(world != null){
            NailedWorld nworld = new NailedWorld(world, context);
            this.vanillaWorlds.put(id, world);
            this.vanillaWorldIdArray = ArrayUtils.toPrimitive(this.vanillaWorlds.keySet().toArray(new Integer[this.vanillaWorlds.size()]));
            this.worlds.put(id, nworld);
            MinecraftServer.getServer().worldTickTimes.put(id, new long[100]);
            logger.info("Loading dimension " + id + " (" + world.getWorldInfo().getWorldName() + ") (" + nworld.toString() + ")");
        }else{
            this.vanillaWorlds.remove(id);
            this.vanillaWorldIdArray = ArrayUtils.toPrimitive(this.vanillaWorlds.keySet().toArray(new Integer[this.vanillaWorlds.size()]));
            this.worlds.remove(id);
            MinecraftServer.getServer().worldTickTimes.remove(id);
            logger.info("Unloading dimension " + id);
        }
        List<WorldServer> builder = new ArrayList<WorldServer>();
        if(this.vanillaWorlds.get(0) != null){
            builder.add(vanillaWorlds.get(0));
        }
        for(Map.Entry<Integer, WorldServer> e : this.vanillaWorlds.entrySet()){
            int dim = e.getKey();
            if(dim < -1 || dim > 1){
                builder.add(e.getValue());
            }
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

    public NailedWorld getWorld(int dimension){
        return this.worlds.get(dimension);
    }

    public WorldServer[] getVanillaWorlds(){
        return this.vanillaWorlds.values().toArray(new WorldServer[this.vanillaWorlds.size()]);
    }

    public NailedWorld[] getWorlds(){
        return this.worlds.values().toArray(new NailedWorld[this.worlds.size()]);
    }

    public net.minecraft.world.WorldProvider createProviderFor(int dim){
        if(!this.customProviders.containsKey(dim)){
            throw new RuntimeException(String.format("No WorldProvider bound for dimension %d", dim));
        }
        DelegatingWorldProvider d = new DelegatingWorldProvider(this.customProviders.get(dim));
        d.setDimension(dim);
        return d;
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

    public static NailedDimensionManager instance() {
        return INSTANCE;
    }
}
