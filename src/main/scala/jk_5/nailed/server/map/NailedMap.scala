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

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World
import jk_5.nailed.server.map.game.NailedGameManager
import jk_5.nailed.server.map.stat.NailedStatManager
import jk_5.nailed.server.scoreboard.MapScoreboardManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedMap(override val id: Int, override val mappack: Mappack = null, private val baseDir: File) extends Map with TeamManager {

  private val playerSet = mutable.HashSet[Player]()
  var players = new Array[Player](0)
  override val getScoreboardManager = new MapScoreboardManager(this)
  override val getGameManager = new NailedGameManager(this)
  override val getStatManager = new NailedStatManager(this)

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
    players = playerSet.toArray
    getScoreboardManager.onPlayerJoined(player)
    this.playerJoined(player)
  }

  override def onPlayerLeft(player: Player){
    playerSet -= player
    players = playerSet.toArray
    getScoreboardManager.onPlayerLeft(player)
    this.playerLeft(player)
  }

  override def broadcastChatMessage(message: BaseComponent) = playerSet.foreach(_.sendMessage(message))
  override def broadcastChatMessage(message: BaseComponent*) = playerSet.foreach(_.sendMessage(message: _*))
  override def broadcastChatMessage(message: Array[BaseComponent]) = playerSet.foreach(_.sendMessage(message))

  override def toString = s"NailedMap{id=$id,mappack=$mappack}"
}
