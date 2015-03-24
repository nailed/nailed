package jk_5.nailed.server.map;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.teleport.TeleportEventEnd;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.MapLoader;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.mappack.MappackLoadingFailedException;
import jk_5.nailed.api.mappack.metadata.MappackWorld;
import jk_5.nailed.api.world.Dimension;
import jk_5.nailed.api.world.World;
import jk_5.nailed.api.world.WorldContext;
import jk_5.nailed.api.world.WorldProvider;
import jk_5.nailed.server.NailedEventFactory;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.event.EntityDamageEvent;
import jk_5.nailed.server.event.EntityFallEvent;
import jk_5.nailed.server.mappack.NailedMappackRegistry;
import jk_5.nailed.server.scheduler.NailedScheduler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NailedMapLoader implements MapLoader {

    private static final NailedMapLoader INSTANCE = new NailedMapLoader();
    private static final Logger logger = LogManager.getLogger();

    private final TIntObjectHashMap<Map> maps = new TIntObjectHashMap<Map>();
    private final java.util.Map<Map, List<World>> mapWorlds = new HashMap<Map, List<World>>();
    private final AtomicInteger nextMapId = new AtomicInteger(0);
    private final File mapsDir = new File("maps");

    private Mappack lobbyMappack;
    private Map lobby;

    @Nonnull
    @Override
    public Mappack getLobbyMappack() {
        String lobbyName = NailedPlatform.instance().getConfig().getString("lobbyMappack");
        Mappack mp = NailedMappackRegistry.instance().getByName(lobbyName);
        if(mp == null){
            logger.warn("Mappack {} was defined as a lobby mappack in the config, but it does not exist. Falling back to a dummy", lobbyName);
            this.lobbyMappack = DummyLobbyMappack.instance();
        }else{
            this.lobbyMappack = mp;
        }
        return this.lobbyMappack;
    }

    @Nonnull
    @Override
    public Map getLobby() {
        return this.lobby;
    }

    @Nullable
    @Override
    public Map getMap(int mapid) {
        return this.maps.get(mapid);
    }

    public NailedMap registerMap(NailedMap map){
        if(map.id() == 0){
            this.lobby = map;
        }
        this.maps.put(map.id(), map);
        logger.info("Registered {}", map);
        return map;
    }

    public Map createLobbyMap(){
        int id = nextMapId.getAndIncrement();
        final Promise<Void> finishPromise = new DefaultPromise<Void>(NailedScheduler.instance().getExecutor().next());
        final File baseDir = new File(mapsDir, "lobby");
        NailedScheduler.instance().submit(new Runnable() {
            @Override
            public void run() {
                baseDir.mkdir();
                getLobbyMappack().prepareWorld(baseDir, finishPromise);
            }
        });
        try{
            finishPromise.get();
        }catch(Exception e){
            logger.error("Exception while waiting for the promise to finish", e);
        }
        NailedMap map = new NailedMap(id, getLobbyMappack(), baseDir);
        if(finishPromise.isSuccess()){
            this.registerMap(map);
            this.loadMappackWorlds(map, getLobbyMappack(), "lobby");
            return map;
        }else{
            logger.warn("Loading of map {} with mappack {} failed.", map, getLobbyMappack());
            throw new MappackLoadingFailedException("Map loading failed", finishPromise.cause());
        }
    }

    @Nonnull
    @Override
    public Future<Map> createMapFor(@Nonnull final Mappack mappack) {
        final int id = nextMapId.getAndIncrement();
        final Promise<Map> allDonePromise = new DefaultPromise<Map>(NailedScheduler.instance().getExecutor().next());
        final Promise<Void> finishPromise = new DefaultPromise<Void>(NailedScheduler.instance().getExecutor().next());
        finishPromise.addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future){
                if(future.isSuccess()){
                    NailedScheduler.instance().executeSync(new Runnable() {
                        @Override
                        public void run() {
                            NailedMap map = new NailedMap(id, mappack, new File(mapsDir, "map_" + id));
                            registerMap(map);
                            loadMappackWorlds(map, mappack, "map_" + id);
                            allDonePromise.setSuccess(map);
                        }
                    });
                }else{
                    logger.warn("Loading of map " + mappack.getId() + "_" + id + " with mappack " + mappack.toString() + " failed. ", future.cause());
                }
            }
        });
        NailedScheduler.instance().submit(new Runnable(){
            @Override
            public void run(){
                File dir = new File(mapsDir, "map_" + id);
                dir.mkdir();
                mappack.prepareWorld(dir, finishPromise);
            }
        });
        return allDonePromise;
    }

    private void loadMappackWorlds(Map map, Mappack mappack, String saveDir){
        for(final MappackWorld world : mappack.getMetadata().worlds()){
            WorldProvider provider = new WorldProvider() {
                private int id;

                @Override
                public int getId() {
                    return id;
                }

                @Override
                public void setId(int id) {
                    this.id = id;
                }

                @Nonnull
                @Override
                public Dimension getDimension() {
                    return world.dimension();
                }

                @Nonnull
                @Override
                public String getType() {
                    return world.generator();
                }

                @Nonnull
                @Override
                public String getOptions() {
                    return null;
                }
            };
            map.addWorld(NailedPlatform.instance().createNewWorld(provider, new WorldContext(saveDir, world.name(), world)));
        }
    }

    public void addWorldToMap(World world, Map map){
        List<World> list = this.mapWorlds.get(map);
        if(list == null){
            list = new ArrayList<World>();
            this.mapWorlds.put(map, list);
        }
        list.add(world);
        world.setMap(map);
        logger.info("World {} was added to {}", world, map);
    }

    public List<World> getWorldsForMap(Map map){
        return this.mapWorlds.get(map);
    }

    @EventHandler
    public void onPlayerTeleported(TeleportEventEnd event){
        World oldWorld = event.getOldWorld();
        World newWorld = event.getNewWorld();
        Map oldMap = oldWorld.getMap();
        Map newMap = newWorld.getMap();
        if(oldWorld != newWorld){
            oldWorld.onPlayerLeft(event.getPlayer());
            NailedEventFactory.firePlayerLeftWorld(event.getPlayer(), oldWorld);
            newWorld.onPlayerJoined(event.getPlayer());
            NailedEventFactory.firePlayerJoinWorld(event.getPlayer(), newWorld);
        }
        if(oldMap != null || newMap != null){
            if(oldMap != null && newMap != null){
                if(oldMap != newMap){
                    if(oldMap instanceof NailedMap){
                        ((NailedMap) oldMap).onPlayerLeft(event.getPlayer());
                    }
                    NailedEventFactory.firePlayerLeftMap(event.getPlayer(), oldMap);
                    if(newMap instanceof NailedMap){
                        ((NailedMap) newMap).onPlayerJoined(event.getPlayer());
                    }
                    NailedEventFactory.firePlayerJoinMap(event.getPlayer(), newMap);
                }
            }else{
                if(oldMap != null){
                    if(oldMap instanceof NailedMap){
                        ((NailedMap) oldMap).onPlayerLeft(event.getPlayer());
                    }
                    NailedEventFactory.firePlayerLeftMap(event.getPlayer(), oldMap);
                }
                if(newMap != null){
                    if(newMap instanceof NailedMap){
                        ((NailedMap) newMap).onPlayerJoined(event.getPlayer());
                    }
                    NailedEventFactory.firePlayerJoinMap(event.getPlayer(), newMap);
                }
            }
        }
    }

    @EventHandler
    public void onEntityFall(EntityFallEvent event){
        if(event.getEntity() instanceof EntityPlayerMP){
            EntityPlayerMP e = (EntityPlayerMP) event.getEntity();
            World world = NailedPlatform.instance().getPlayerFromEntity(e).getWorld();
            if(world.getConfig() != null && world.getConfig().disableDamage()){
                event.setCanceled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getSource() == DamageSource.outOfWorld) return;
        if(event.getEntity() instanceof EntityPlayerMP){
            EntityPlayerMP e = (EntityPlayerMP) event.getEntity();
            World world = NailedPlatform.instance().getPlayerFromEntity(e).getWorld();
            if(world.getConfig() != null && world.getConfig().disableDamage()){
                event.setCanceled(true);
            }
        }
    }

    //TODO
      /*@EventHandler
      def onBlockPlace(event: BlockPlaceEvent){
        if(event.world.getConfig != null && event.world.getConfig.disableBlockPlacement){
          event.setCanceled(true)
        }
      }

      @EventHandler
      def onBlockBreak(event: BlockBreakEvent){
        if(event.world.getConfig != null && event.world.getConfig.disableBlockBreaking){
          event.setCanceled(true)
        }
      }*/

    public static NailedMapLoader instance(){
        return INSTANCE;
    }
}
