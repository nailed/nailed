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

package jk_5.nailed.server.player

import java.util.{Collections, UUID}

import com.google.common.collect.ImmutableSet
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.Platform
import jk_5.nailed.api.event.player.{PlayerJoinServerEvent, PlayerLeaveServerEvent}
import jk_5.nailed.api.player.Player
import net.minecraft.entity.player.EntityPlayerMP
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
trait PlayerRegistry extends Platform {

  private val players = mutable.ArrayBuffer[NailedPlayer]()
  private var onlinePlayers = new Array[NailedPlayer](0)
  private var onlinePlayersCollection: java.util.Collection[Player] = Collections.emptyList()
  private val playersById = mutable.HashMap[UUID, NailedPlayer]()
  private val playersByName = mutable.HashMap[String, NailedPlayer]()
  private val logger = LogManager.getLogger

  override def getPlayer(id: UUID) = this.playersById.get(id).orNull
  override def getPlayerByName(name: String) = this.playersByName.get(name).orNull
  def getPlayerFromEntity(entity: EntityPlayerMP) = this.getPlayer(entity.getGameProfile.getId)

  def getOrCreatePlayer(entity: EntityPlayerMP): NailedPlayer = this.getPlayer(entity.getGameProfile.getId) match {
    case null =>
      val player = new NailedPlayer(entity.getGameProfile.getId, entity.getGameProfile.getName)
      this.players += player
      this.playersById.put(entity.getGameProfile.getId, player)
      this.playersByName.put(entity.getGameProfile.getName, player)
      player
    case p => p.asInstanceOf[NailedPlayer]
  }

  override def getOnlinePlayers: java.util.Collection[Player] = this.onlinePlayersCollection

  @EventHandler
  def onPlayerJoin(event: PlayerJoinServerEvent){
    val b = mutable.ArrayBuffer[NailedPlayer]()
    b ++= this.onlinePlayers
    b += event.getPlayer.asInstanceOf[NailedPlayer]
    this.onlinePlayers = b.toArray
    this.onlinePlayersCollection = ImmutableSet.copyOf(this.onlinePlayers.asInstanceOf[Array[Player]])
  }

  @EventHandler
  def onPlayerLeave(event: PlayerLeaveServerEvent){
    this.onlinePlayers = this.onlinePlayers.filter(p => p != event.getPlayer)
    this.onlinePlayersCollection = ImmutableSet.copyOf(this.onlinePlayers.asInstanceOf[Array[Player]])
  }
}
