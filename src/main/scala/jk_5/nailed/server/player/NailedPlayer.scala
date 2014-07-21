package jk_5.nailed.server.player

import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World
import jk_5.nailed.server.teleport.Teleporter
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.play.server.S02PacketChat

/**
 * No description given
 *
 * @author jk-5
 */
class NailedPlayer(private val uuid: UUID, private var name: String) extends Player {

  var entity: EntityPlayerMP = _
  var world: World = _
  var map: Map = _
  private var displayName: String = this.name
  var netHandler: NetHandlerPlayServer = _
  var isOnline: Boolean = false

  override def getName = this.name
  override def getDisplayName = this.displayName
  override def getUniqueId = this.uuid
  override def hasPermission(permission: String) = true //TODO
  override def sendMessage(message: BaseComponent) = this.netHandler.sendPacket(new S02PacketChat(message))
  override def sendMessage(messages: BaseComponent*) = this.netHandler.sendPacket(new S02PacketChat(messages: _*))
  override def sendMessage(messages: Array[BaseComponent]) = this.netHandler.sendPacket(new S02PacketChat(messages: _*))
  override def getPlayer = this
  override def getLastPlayed: Long = 0
  override def hasPlayedBefore: Boolean = false
  override def getFirstPlayed: Long = 0
  override def isBanned: Boolean = false

  override def teleportTo(world: World){
    Teleporter.teleportPlayer(this, new TeleportOptions(if(world.getConfig != null) {val s = world.getConfig.spawnPoint; s.setWorld(world); s} else new Location(world, 0, 64, 0)))
  }

  def getEntity = this.entity
  def getWorld = this.world
  def getMap = this.map
  def getLocation = new Location(this.world, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)

  override def toString = s"NailedPlayer{uuid=$uuid,name=$name,isOnline=$isOnline}"
}
