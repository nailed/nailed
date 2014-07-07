package jk_5.nailed.server.map

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import io.netty.util.concurrent.{DefaultPromise, Future, FutureListener}
import jk_5.nailed.api
import jk_5.nailed.api.map.{Map, MapLoader, MappackLoadingFailedException}
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.world.WorldContext
import jk_5.nailed.server.NailedServer
import jk_5.nailed.server.scheduler.NailedScheduler
import jk_5.nailed.server.world.NailedDefaultWorldProviders
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedMapLoader extends MapLoader {

  private var lobbyMappack: Mappack = _
  private var lobby: Map = _
  private val nextMapId = new AtomicInteger(0)
  private val maps = mutable.HashMap[Int, NailedMap]()
  private val mapWorlds = mutable.HashMap[Map, mutable.ArrayBuffer[api.world.World]]()
  private val logger = LogManager.getLogger
  private val mapsDir = new File("maps")

  override def setLobbyMappack(mappack: Mappack): Boolean = {
    logger.info("Lobby mappack was set to {}", mappack)
    if(this.lobbyMappack != null) false
    else{
      this.lobbyMappack = mappack
      true
    }
  }
  override def getLobbyMappack = this.lobbyMappack
  override def getLobby: Map = {
    if(this.lobby == null){
      //TODO: this is not correct!
      this.lobby = new NailedMap(nextMapId.getAndIncrement, this.lobbyMappack)
    }
    this.lobby
  }
  override def getMap(mapId: Int) = this.maps.get(mapId)
  override def getOrCreateMap(mapId: Int): Map = {
    this.maps.get(mapId) match {
      case Some(map) =>
        map
      case None =>
        val m = new NailedMap(nextMapId.getAndIncrement)
        this.addMap(m)
        m
    }
  }

  def addMap(map: NailedMap): NailedMap = {
    if(map.getId == 0) this.lobby = map
    this.maps.put(map.getId, map)
    logger.info("Registered {}", map.toString)
    map
  }

  def createLobbyMap(): Map = {
    val id = nextMapId.getAndIncrement
    val finishPromise = new DefaultPromise[Void](NailedScheduler.executor.next())
    NailedScheduler.submit(new Runnable(){
      override def run(){
        val dir = new File(mapsDir, "lobby")
        dir.mkdir()
        lobbyMappack.prepareWorld(dir, finishPromise)
      }
    })
    finishPromise.get()
    val map = new NailedMap(id, lobbyMappack)
    if(finishPromise.isSuccess){
      //TODO: ask mappacks which worlds should be loaded and add them
      //TODO: add some kind of context to createNewWorld that tells the DimensionManager which world it is currently loading
      //      Using this we can determine where we should save the map
      val world = NailedServer.createNewWorld(NailedDefaultWorldProviders.getVoidProvider, new WorldContext("lobby", "DIM0"))
      map.addWorld(world)
      addMap(map)
      map
    }else{
      logger.warn("Loading of map {} with mappack {} failed.", map, lobbyMappack)
      throw new MappackLoadingFailedException("Map loading failed", finishPromise.cause()) //TODO: custom exception!
    }
  }

  override def createMapFor(mappack: Mappack): Future[Map] = {
    val id = nextMapId.getAndIncrement
    val allDonePromise = new DefaultPromise[Map](NailedScheduler.executor.next())
    val finishPromise = new DefaultPromise[Void](NailedScheduler.executor.next())
    finishPromise.addListener(new FutureListener[Void] {
      override def operationComplete(future: Future[Void]){
        if(future.isSuccess){
          NailedScheduler.executeSync(new Runnable(){
            override def run(){
              val map = new NailedMap(id, mappack)
              val baseDir = "map_" + id
              //TODO: ask mappacks which worlds should be loaded and add them
              //TODO: add some kind of context to createNewWorld that tells the DimensionManager which world it is currently loading
              //      Using this we can determine where we should save the map
              val world = NailedServer.createNewWorld(NailedDefaultWorldProviders.getVoidProvider, new WorldContext(null, "DIM0"))
              map.addWorld(world)
            }
          })
        }else{
          logger.warn("Loading of map " + mappack.getId + "_" + id + " with mappack " + mappack.toString + " failed. ", future.cause())
        }
      }
    })
    NailedScheduler.submit(new Runnable(){
      //TODO: don't use these fixed savedirs
      override def run(){
        val dir = new File(mapsDir, "map_" + mappack.getId + "_" + id)
        dir.mkdir()
        mappack.prepareWorld(dir, finishPromise)
      }
    })
    allDonePromise
  }

  def addWorldToMap(world: api.world.World, map: Map){
    var list: mutable.ArrayBuffer[api.world.World] = this.mapWorlds.get(map) match {
      case Some(l) => l
      case None =>
        val r = mutable.ArrayBuffer[api.world.World]()
        this.mapWorlds.put(map, r)
        r
    }
    list += world
    world.setMap(map)
    logger.info("World {} was added to {}", world.toString, map.toString)
  }

  def getWorldsForMap(map: Map) = this.mapWorlds.get(map)
}
