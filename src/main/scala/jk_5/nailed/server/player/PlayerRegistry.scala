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

import java.util.UUID

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.Server
import jk_5.nailed.api.event.{PlayerJoinServerEvent, PlayerLeaveServerEvent}
import net.minecraft.entity.player.EntityPlayerMP
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
trait PlayerRegistry extends Server {

  private val players = mutable.ArrayBuffer[NailedPlayer]()
  private var onlinePlayers = new Array[NailedPlayer](0)
  private val playersById = mutable.HashMap[UUID, NailedPlayer]()
  private val playersByName = mutable.HashMap[String, NailedPlayer]()
  private val logger = LogManager.getLogger

  /**
   * Gets the player with the given UUID.
   *
   * @param id UUID of the player to retrieve
   * @return Some(player) if a player was found, None otherwise
   */
  override def getPlayer(id: UUID) = this.playersById.get(id)

  @deprecated("Use the new uuid replacement", "mc1.8")
  override def getPlayerByName(name: String) = this.playersByName.get(name)

  def getPlayerFromEntity(entity: EntityPlayerMP) = this.getPlayer(entity.getGameProfile.getId).get

  def getOrCreatePlayer(entity: EntityPlayerMP) = this.getPlayer(entity.getGameProfile.getId) match {
    case Some(player) => player
    case None =>
      val player = new NailedPlayer(entity.getGameProfile.getId, entity.getGameProfile.getName)
      this.players += player
      this.playersById.put(entity.getGameProfile.getId, player)
      this.playersByName.put(entity.getGameProfile.getName, player)
      player
  }

  /**
   * Gets all currently online players
   *
   * @return an array containing all online players
   */
  override def getOnlinePlayers: Array[NailedPlayer] = this.onlinePlayers

  @EventHandler
  def onPlayerJoin(event: PlayerJoinServerEvent){
    this.onlinePlayers = Array[NailedPlayer](this.onlinePlayers: _*, event.player.asInstanceOf[NailedPlayer])
  }

  @EventHandler
  def onPlayerLeave(event: PlayerLeaveServerEvent){
    this.onlinePlayers = this.onlinePlayers.filter(p => p != event.player)
  }
}
