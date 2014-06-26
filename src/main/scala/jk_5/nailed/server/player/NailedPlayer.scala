package jk_5.nailed.server.player

import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.player.Player
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer

/**
 * No description given
 *
 * @author jk-5
 */
class NailedPlayer(private val uuid: UUID, private var name: String) extends Player {

  private var entity: EntityPlayerMP = _
  private var displayName: String = this.name
  private var netHandler: NetHandlerPlayServer = _

  /**
   * Returns the name of this player
   * <p>
   * Names are no longer unique past a single game session. For persistent storage
   * it is recommended that you use {@link #getUniqueId()} instead.
   *
   * @return Player name or null if we have not seen a name for this player yet
   */
  override def getName = this.name

  /**
   * Gets the "friendly" name to display of this player. This may include
   * color.
   * <p>
   * Note that this name will not be displayed in game, only in chat and
   * places defined by plugins.
   *
   * @return the friendly name
   */
  override def getDisplayName = this.displayName

  /**
   * Returns the UUID of this player
   *
   * @return Player UUID
   */
  override def getUniqueId = this.uuid

  /**
   * Checks if this user has the specified permission node.
   *
   * @param permission the node to check
   * @return whether they have this node
   */
  override def hasPermission(permission: String) = true //TODO

  /**
   * Send a message to this sender.
   *
   * @param message the message to send
   */
  override def sendMessage(message: BaseComponent): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: BaseComponent*): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: Array[BaseComponent]): Unit = ???

  /**
   * Gets a {@link Player} object that this represents, if there is one
   * <p>
   * If the player is online, this will return that player. Otherwise,
   * it will return null.
   *
   * @return Online player
   */
  override def getPlayer = this

  /**
   * Gets the last date and time that this player was witnessed on this
   * server.
   * <p>
   * If the player has never played before, this will return 0. Otherwise,
   * it will be the amount of milliseconds since midnight, January 1, 1970
   * UTC.
   *
   * @return Date of last log-in for this player, or 0
   */
  override def getLastPlayed: Long = 0

  /**
   * Checks if this player has played on this server before.
   *
   * @return True if the player has played before, otherwise false
   */
  override def hasPlayedBefore: Boolean = false

  /**
   * Gets the first date and time that this player was witnessed on this
   * server.
   * <p>
   * If the player has never played before, this will return 0. Otherwise,
   * it will be the amount of milliseconds since midnight, January 1, 1970
   * UTC.
   *
   * @return Date of first log-in for this player, or 0
   */
  override def getFirstPlayed: Long = 0

  /**
   * Checks if this player is currently online
   *
   * @return true if they are online
   */
  override def isOnline: Boolean = this.netHandler != null

  /**
   * Checks if this player is banned or not
   *
   * @return true if banned, otherwise false
   */
  override def isBanned: Boolean = false
}
