package jk_5.nailed.server.player

import java.util.UUID

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.Server
import jk_5.nailed.api.event.{PlayerJoinServerEvent, PlayerLeaveServerEvent}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.world.BossBar
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
  private val onlinePlayers = mutable.ArrayBuffer[NailedPlayer]()
  private val playersById = mutable.HashMap[UUID, NailedPlayer]()
  private val logger = LogManager.getLogger

  /**
   * Gets the player with the given UUID.
   *
   * @param id UUID of the player to retrieve
   * @return Some(player) if a player was found, None otherwise
   */
  override def getPlayer(id: UUID): Option[Player] = this.playersById.get(id)

  def getPlayerFromEntity(entity: EntityPlayerMP): Player = this.getPlayer(entity.getGameProfile.getId).get

  def getOrCreatePlayer(entity: EntityPlayerMP): Player = this.getPlayer(entity.getGameProfile.getId) match {
    case Some(player) => player
    case None =>
      val player = new NailedPlayer(entity.getGameProfile.getId, entity.getGameProfile.getName)
      this.players += player
      this.playersById.put(entity.getGameProfile.getId, player)
      player
  }

  /**
   * Gets all currently online players
   *
   * @return an array containing all online players
   */
  override def getOnlinePlayers: Array[Player] = this.onlinePlayers.toArray

  @EventHandler
  def onPlayerJoin(event: PlayerJoinServerEvent){
    this.onlinePlayers += event.player.asInstanceOf[NailedPlayer]
    //logger.info("Player " + event.player.getName + " logged in")

    val player = event.player.asInstanceOf[NailedPlayer]
    val loc = new Location(player.getWorld, player.entity.posX, 200, player.entity.posZ)
    player.netHandler.sendPacket(BossBar.getSpawnPacket("HI FAGGOTS!", loc))
  }

  @EventHandler
  def onPlayerLeave(event: PlayerLeaveServerEvent){
    this.onlinePlayers -= event.player.asInstanceOf[NailedPlayer]
    //logger.info("Player " + event.player.getName + " logged out")
  }
}
