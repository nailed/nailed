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

package jk_5.nailed.api.player

import java.util.UUID

/**
 * No description given
 *
 * @author jk-5
 */
trait OfflinePlayer {

  /**
   * Checks if this player is currently online
   *
   * @return true if they are online
   */
  def isOnline: Boolean

  /**
   * Returns the name of this player
   * <p>
   * Names are no longer unique past a single game session. For persistent storage
   * it is recommended that you use {@link #getUniqueId()} instead.
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
   * Checks if this player is banned or not
   *
   * @return true if banned, otherwise false
   */
  def isBanned: Boolean

  /**
   * Gets a {@link Player} object that this represents, if there is one
   * <p>
   * If the player is online, this will return that player. Otherwise,
   * it will return null.
   *
   * @return Online player
   */
  def getPlayer: Player

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
  def getFirstPlayed: Long

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
  def getLastPlayed: Long

  /**
   * Checks if this player has played on this server before.
   *
   * @return True if the player has played before, otherwise false
   */
  def hasPlayedBefore: Boolean
}
