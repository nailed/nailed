package jk_5.nailed.api.player

import java.util.UUID

import jk_5.nailed.api.command.{CommandSender, WorldCommandSender}
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World

/**
 * Represents a player, connected or not
 *
 * @author jk-5
 */
trait Player extends CommandSender with OfflinePlayer with WorldCommandSender {

  /**
   * Returns the name of this player
   * <p>
   * Names are no longer unique past a single game session. For persistent storage
   * it is recommended that you use `getUniqueId` instead.
   *
   * @return Player name or null if we have not seen a name for this player yet
   */
  def getName: String

  /**
   * Returns the UUID of this player
   *
   * @return Player UUID
   */
  def getUniqueId: UUID

  /**
   * Gets the "friendly" name to display of this player. This may include
   * color.
   * <p>
   * Note that this name will not be displayed in game, only in chat and
   * places defined by plugins.
   *
   * @return the friendly name
   */
  def getDisplayName: String

  def teleportTo(world: World)

  def getWorld: World

  def getLocation: Location
}
