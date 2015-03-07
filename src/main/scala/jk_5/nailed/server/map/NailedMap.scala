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
import java.util.Collections

import jk_5.nailed.api.GameMode
import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World
import jk_5.nailed.server.map.game.NailedGameManager
import jk_5.nailed.server.map.game.script.api.{EventEmitter, ScriptPlayerApi}
import jk_5.nailed.server.map.stat.NailedStatManager
import jk_5.nailed.server.player.NailedPlayer
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
  override val getGameManager = new NailedGameManager(this)
  override val getStatManager = new NailedStatManager(this)
  var defaultWorld: World = null
  var eventEmitter: EventEmitter = null

  this.init() //Init the TeamManager

  override def players = java.util.Arrays.asList(playerSet.toArray: _*) //TODO: cache array

  override def worlds: java.util.Collection[World] = NailedMapLoader.getWorldsForMap(this) match {
    case Some(s) => java.util.Arrays.asList(s.toArray: _*)
    case None => Collections.emptyList()
  }

  override def worldsArray = NailedMapLoader.getWorldsForMap(this) match {
    case Some(s) => s.toArray
    case None => new Array[World](0)
  }

  override def addWorld(world: World){
    NailedMapLoader.addWorldToMap(world, this)
    if(world.getConfig.isDefault) defaultWorld = world
  }

  def onPlayerJoined(player: Player){
    playerSet += player
    getScoreboardManager.onPlayerJoined(player)
    this.playerJoined(player)
    if(eventEmitter != null) eventEmitter.emit("playerJoinedMap", new ScriptPlayerApi(player.asInstanceOf[NailedPlayer]))
  }

  def onPlayerLeft(player: Player){
    playerSet -= player
    getScoreboardManager.onPlayerLeft(player)
    this.playerLeft(player)
    if(eventEmitter != null) eventEmitter.emit("playerLeftMap", new ScriptPlayerApi(player.asInstanceOf[NailedPlayer]))

    //TODO: move this to a per-map inventory system
    //See https://github.com/nailed/nailed/issues/50
    player.clearInventory()
    player.setGameMode(GameMode.ADVENTURE)
    player.setAllowedToFly(false)
    player.clearTitle()
    player.clearSubtitle()
    player.clearAllEffects()
  }

  override def broadcastChatMessage(message: BaseComponent*) = playerSet.foreach(_.sendMessage(message: _*))

  override def toString = s"NailedMap{id=$id,mappack=$mappack}"
}
