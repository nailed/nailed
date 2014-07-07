package jk_5.nailed.server.map

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import io.netty.util.concurrent.{DefaultPromise, Future, FutureListener}
import jk_5.nailed.api
import jk_5.nailed.api.map.{Map, MapLoader, MappackLoadingFailedException}
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.world.{WorldContext, WorldProvider}
import jk_5.nailed.server.NailedServer
import jk_5.nailed.server.scheduler.NailedScheduler
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
  override def getLobby = this.lobby
  override def getMap(mapId: Int) = this.maps.get(mapId)
  override def getOrCreateMap(mapId: Int): Map = {
    this.maps.get(mapId) match {
      case Some(map) =>
        map
      case None =>
        throw new RuntimeException("No map exists for mapid " + mapId)
        //val m = new NailedMap(nextMapId.getAndIncrement)
        //this.addMap(m)
        //m
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
    val baseDir = new File(mapsDir, "lobby")
    NailedScheduler.submit(new Runnable(){
      override def run(){
        baseDir.mkdir()
        lobbyMappack.prepareWorld(baseDir, finishPromise)
      }
    })
    finishPromise.get()
    val map = new NailedMap(id, lobbyMappack, baseDir)
    if(finishPromise.isSuccess){
      addMap(map)
      loadMappackWorlds(map, lobbyMappack, "lobby")
      map
    }else{
      logger.warn("Loading of map {} with mappack {} failed.", map, lobbyMappack)
      throw new MappackLoadingFailedException("Map loading failed", finishPromise.cause())
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
              val map = new NailedMap(id, mappack, new File(mapsDir, "map_" + id))
              addMap(map)
              loadMappackWorlds(map, mappack, "map_" + id)
            }
          })
        }else{
          logger.warn("Loading of map " + mappack.getId + "_" + id + " with mappack " + mappack.toString + " failed. ", future.cause())
        }
      }
    })
    NailedScheduler.submit(new Runnable(){
      override def run(){
        val dir = new File(mapsDir, "map_" + id)
        dir.mkdir()
        mappack.prepareWorld(dir, finishPromise)
      }
    })
    allDonePromise
  }

  private def loadMappackWorlds(map: Map, mappack: Mappack, saveDir: String){
    for(w <- mappack.getMetadata.worlds){
      val provider = new WorldProvider {
        var id: Int = _
        override def getType = w.generator
        override def getOptions = null
        override def getId = this.id
        override def setId(id: Int) = this.id = id
        override def getTypeId = w.dimension
      }
      map.addWorld(NailedServer.createNewWorld(provider, new WorldContext(saveDir, w.name)))
    }
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
