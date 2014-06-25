package jk_5.nailed.api.player

import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.world.{Dimension, World}
import net.minecraft.entity.player.EntityPlayer
import java.util.UUID

/**
 * No description given
 *
 * @author mattashii
 */
trait Player extends CommandSender{
  /**
   * Get the chat prefix of this player.
   *
   * @return the senders chat prefix
   */
  def getPrefix: String

  /**
   * Get the Player Entity.
   *
   * @return the players EntityPlayer
   */
  def getEntity: EntityPlayer

  /**
   * Get the UUID of the player.
   *
   * @return the senders UUID
   */
  def getUUID: UUID

  /**
   * Get the world the player is in.
   *
   * @return the world the player is in
   */
  def getWorld: World

  /**
   * Get the dimension the player is in.
   *
   * @return the dimension the player is in
   */
  def getDimension: Dimension
}
