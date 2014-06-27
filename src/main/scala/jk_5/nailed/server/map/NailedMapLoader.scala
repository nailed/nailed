package jk_5.nailed.server.map

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import io.netty.util.concurrent.{DefaultPromise, Future, FutureListener}
import jk_5.nailed.api.map.{Map, MapLoader}
import jk_5.nailed.api.mappack.Mappack
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
  private val logger = LogManager.getLogger
  private val mapsDir = new File("maps")

  override def setLobbyMappack(mappack: Mappack): Boolean = {
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
      this.lobby = new NailedMap(this.lobbyMappack)
    }
    this.lobby
  }
  override def getMap(dimension: Int): Map = {
    this.maps.get(dimension) match {
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
    logger.info("Registered " + map.getSaveFolderName)
    map
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
              //TODO: ask mappacks which worlds should be loaded and add them
              val world = NailedServer.createNewWorld(NailedDefaultWorldProviders.getVoidProvider)
              map.addWorld(world)
            }
          })
        }else{
          logger.warn("Loading of map " + mappack.getId + "_" + id + " failed. ", future.cause())
        }
      }
    })
    NailedScheduler.submit(new Runnable(){
      override def run() = mappack.prepareWorld(new File(mapsDir, "map_" + mappack.getId + "_" + id), finishPromise)
    })
    allDonePromise
  }
}
