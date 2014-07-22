package jk_5.nailed.server.map

import java.io.File

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World
import jk_5.nailed.server.scoreboard.MapScoreboardManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedMap(override val id: Int, override val mappack: Mappack = null, private val baseDir: File) extends Map with TeamManager {

  private val playerSet = mutable.HashSet[Player]()
  override val getScoreboardManager = new MapScoreboardManager(this)

  this.init() //Init the TeamManager

  override def worlds: Array[World] = NailedMapLoader.getWorldsForMap(this) match {
    case Some(s) => s.toArray
    case None => new Array[World](0)
  }

  override def addWorld(world: World){
    NailedMapLoader.addWorldToMap(world, this)
  }

  override def onPlayerJoined(player: Player){
    playerSet += player
    getScoreboardManager.onPlayerJoined(player)
    this.playerJoined(player)
  }

  override def onPlayerLeft(player: Player){
    playerSet -= player
    getScoreboardManager.onPlayerLeft(player)
    this.playerLeft(player)
  }

  override def broadcastChatMessage(message: BaseComponent) = playerSet.foreach(_.sendMessage(message))
  override def broadcastChatMessage(message: BaseComponent*) = playerSet.foreach(_.sendMessage(message: _*))
  override def broadcastChatMessage(message: Array[BaseComponent]) = playerSet.foreach(_.sendMessage(message))

  override def players = this.playerSet.toArray

  override def toString = s"NailedMap{id=$id,mappack=$mappack}"
}
