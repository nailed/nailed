/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.map

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import io.netty.util.concurrent.{DefaultPromise, Future, FutureListener}
import jk_5.eventbus.EventHandler
import jk_5.nailed.api
import jk_5.nailed.api.event.teleport.TeleportEventEnd
import jk_5.nailed.api.map.{Map, MapLoader}
import jk_5.nailed.api.mappack.{Mappack, MappackLoadingFailedException}
import jk_5.nailed.api.world.{WorldContext, WorldProvider}
import jk_5.nailed.server.event.{EntityDamageEvent, EntityFallEvent}
import jk_5.nailed.server.mappack.NailedMappackRegistry
import jk_5.nailed.server.scheduler.NailedScheduler
import jk_5.nailed.server.{NailedEventFactory, NailedPlatform}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.DamageSource
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

  def checkLobbyMappack(){
    setLobbyMappack(NailedMappackRegistry.getByName(NailedPlatform.config.getString("lobbyMappack")))
  }

  def setLobbyMappack(mappack: Mappack): Boolean = {
    logger.info("Lobby mappack was set to {}", mappack)
    if(this.lobbyMappack != null) false
    else{
      this.lobbyMappack = mappack
      true
    }
  }
  override def getLobbyMappack = this.lobbyMappack
  override def getLobby = this.lobby
  override def getMap(mapId: Int) = this.maps.get(mapId).orNull
  override def getOrCreateMap(mapId: Int): Map = {
    this.maps.get(mapId) match {
      case Some(map) => map
      case None => throw new RuntimeException("No map exists for mapid " + mapId)
    }
  }

  def addMap(map: NailedMap): NailedMap = {
    if(map.id == 0) this.lobby = map
    this.maps.put(map.id, map)
    logger.info("Registered {}", map.toString)
    map
  }

  def createLobbyMap(): Map = {
    if(lobbyMappack == null){
      logger.warn("A lobby mappack was not registered yet, but we need it now. Setting it to a default void mappack")
      this.setLobbyMappack(DummyLobbyMappack)
    }

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
              allDonePromise.setSuccess(map)
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
        override def getDimension = w.dimension
      }
      map.addWorld(NailedPlatform.createNewWorld(provider, new WorldContext(saveDir, w.name, w)))
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

  @EventHandler
  def onPlayerTeleported(event: TeleportEventEnd){
    val oldWorld = event.getOldWorld
    val newWorld = event.getNewWorld
    val oldMap = Option(oldWorld.getMap)
    val newMap = Option(newWorld.getMap)
    if(oldWorld != newWorld){
      oldWorld.onPlayerLeft(event.getPlayer)
      NailedEventFactory.firePlayerLeftWorld(event.getPlayer, oldWorld)
      newWorld.onPlayerJoined(event.getPlayer)
      NailedEventFactory.firePlayerJoinWorld(event.getPlayer, newWorld)
    }
    if(oldMap.isDefined || newMap.isDefined){
      if(oldMap.isDefined && newMap.isDefined){
        if(oldMap.get != newMap.get){
          oldMap.get match {
            case m: NailedMap => m.onPlayerLeft(event.getPlayer)
            case _ =>
          }
          NailedEventFactory.firePlayerLeftMap(event.getPlayer, oldMap.get)
          newMap.get match {
            case m: NailedMap => m.onPlayerJoined(event.getPlayer)
            case _ =>
          }
          NailedEventFactory.firePlayerJoinMap(event.getPlayer, newMap.get)
        }
      }else{
        if(oldMap.isDefined){
          oldMap.get match {
            case m: NailedMap => m.onPlayerLeft(event.getPlayer)
            case _ =>
          }
          NailedEventFactory.firePlayerLeftMap(event.getPlayer, oldMap.get)
        }
        if(newMap.isDefined){
          newMap.get match {
            case m: NailedMap => m.onPlayerJoined(event.getPlayer)
            case _ =>
          }
          NailedEventFactory.firePlayerJoinMap(event.getPlayer, newMap.get)
        }
      }
    }
  }

  @EventHandler
  def onEntityFall(event: EntityFallEvent){
    event.entity match {
      case e: EntityPlayerMP =>
        val world = NailedPlatform.getPlayerFromEntity(e).getWorld
        if(world.getConfig != null && world.getConfig.disableDamage){
          event.setCanceled(true)
        }
      case _ =>
    }
  }

  @EventHandler
  def onEntityDamage(event: EntityDamageEvent){
    if(event.source == DamageSource.outOfWorld) return
    event.entity match {
      case e: EntityPlayerMP =>
        val world = NailedPlatform.getPlayerFromEntity(e).getWorld
        if(world.getConfig != null && world.getConfig.disableDamage){
          event.setCanceled(true)
        }
      case _ =>
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
}
