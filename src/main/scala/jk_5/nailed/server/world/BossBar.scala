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

package jk_5.nailed.server.world

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.teleport.TeleportEventExitWorld
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.entity.DataWatcher
import net.minecraft.network.play.server.{S0FPacketSpawnMob, S13PacketDestroyEntities, S1CPacketEntityMetadata}

/**
 * No description given
 *
 * @author jk-5
 */
object BossBar {

  private final val entityId = 1234
  private final val typeId = 63 //63 = dragon. 64 = wither

  def getSpawnPacket(text: String, location: Location): S0FPacketSpawnMob = {
    val packet = new S0FPacketSpawnMob
    packet.entityId = entityId
    packet.`type` = typeId
    packet.x = Math.floor(location.getFloorX * 32.0D).toInt
    packet.y = Math.floor(location.getFloorY * 32.0D).toInt
    packet.z = Math.floor(location.getFloorZ * 32.0D).toInt
    packet.velocityX = 0
    packet.velocityY = 0
    packet.velocityZ = 0
    packet.yaw = 0
    packet.pitch = 0
    packet.headPitch = 0
    packet.field_149043_l = getWatcher(text, 200)
    packet
  }

  def getDestroyPacket = new S13PacketDestroyEntities(entityId)

  def getUpdatePacket(text: String, health: Int) = new S1CPacketEntityMetadata(entityId, getWatcher(text, health), true)

  private def getWatcher(text: String, health: Int): DataWatcher = {
    val watcher = new DataWatcher(null)
    watcher.addObject(0, 0x20.toByte) //Flags. 0x20 = invisible
    watcher.addObject(6, health.toFloat)
    watcher.addObject(10, text); //Entity name
    watcher.addObject(11, 1.toByte); //Show name, 1 = show, 0 = don't show
    watcher
  }

  @EventHandler
  def onPlayerExitWorld(event: TeleportEventExitWorld){
    event.getPlayer.asInstanceOf[NailedPlayer].getEntity.playerNetServerHandler.sendPacket(this.getDestroyPacket)
  }
}
