package jk_5.nailed.server.world

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.{PlayerJoinServerEvent, PlayerLeaveServerEvent, TeleportEventEnterWorld, TeleportEventExitWorld}
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
    packet.field_149042_a = entityId
    packet.field_149040_b = typeId
    packet.field_149041_c = Math.floor(location.getBlockX * 32.0D).toInt
    packet.field_149038_d = Math.floor(location.getBlockY * 32.0D).toInt
    packet.field_149039_e = Math.floor(location.getBlockZ * 32.0D).toInt
    packet.field_149036_f = 0
    packet.field_149037_g = 0
    packet.field_149047_h = 0
    packet.field_149048_i = 0
    packet.field_149045_j = 0
    packet.field_149046_k = 0
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
  def onPlayerJoinServer(event: PlayerJoinServerEvent){
    event.player.asInstanceOf[NailedPlayer].getEntity.playerNetServerHandler.sendPacket(this.getSpawnPacket("test", event.player.getLocation.add(0, 64, 0)))
  }

  @EventHandler
  def onPlayerLeaveServer(event: PlayerLeaveServerEvent){

  }

  @EventHandler
  def onPlayerJoinWorld(event: TeleportEventEnterWorld){
    event.entity.asInstanceOf[NailedPlayer].getEntity.playerNetServerHandler.sendPacket(this.getSpawnPacket("test", event.entity.getLocation.add(0, 64, 0)))
  }

  @EventHandler
  def onPlayerExitWorld(event: TeleportEventExitWorld){
    event.entity.asInstanceOf[NailedPlayer].getEntity.playerNetServerHandler.sendPacket(this.getDestroyPacket)
  }
}
