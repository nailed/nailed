package jk_5.nailed.server.player

import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.player.Player
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.server.MinecraftServer

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class NailedPlayer(private val uuid: UUID, private var name: String) extends Player {

  private var entity: EntityPlayerMP = _
  private var displayName: String = this.name
  private var netHandler: NetHandlerPlayServer = _
  private var nextWorldUUID: UUID = _

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
  override def sendMessage(message: BaseComponent): Unit = {}

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: BaseComponent*): Unit = {}

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: Array[BaseComponent]): Unit = {}

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

  /**
   * Gets the world the player should be sent to when he logs in.
   * <p>
   * To comply with the new EULA changes (june 2014) players have to rejoin to get
   * premium possibilities such as private worlds. This function makes this
   * possible by making it able to store the World object it should be sent to.
   * <p>
   * The UUID of the next world is saved
   *
   * @return the World the player has to be sent to, null if that has been unloaded
   */
  override def getNextWorld: UUID = this.nextWorldUUID

  override def teleportTo(dimension: Int){
    //TODO: temp code!
    val p = MinecraftServer.getServer.getConfigurationManager.playerEntityList.map(_.asInstanceOf[EntityPlayerMP]).find(_.getGameProfile.getId == this.uuid)
    if(p.isDefined){
      p.get.travelToDimension(dimension)
    }
  }

  //TODO: temp code!
  def getEntity = this.entity
}
