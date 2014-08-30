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

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.command.{CommandSender, LocationCommandSender, WorldCommandSender}
import jk_5.nailed.api.entity.Damageable
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.material.ItemStack
import jk_5.nailed.api.scoreboard.ScoreboardManager
import jk_5.nailed.api.util.{Location, Potion}
import jk_5.nailed.api.world.World

/**
 * Represents a player, connected or not
 *
 * @author jk-5
 */
trait Player
  extends CommandSender
  with OfflinePlayer
  with WorldCommandSender
  with LocationCommandSender
  with Damageable
{

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
  def getMap: Map

  def getLocation: Location

  def getScoreboardManager: ScoreboardManager

  def getGameMode: GameMode
  def setGameMode(gm: GameMode)

  def isAllowedToFly: Boolean
  def setAllowedToFly(allowed: Boolean)

  def getInventorySize: Int
  def getInventorySlotContent(slot: Int): ItemStack
  def setInventorySlot(slot: Int, stack: ItemStack)
  def addToInventory(stack: ItemStack)
  def iterateInventory(p: ItemStack => Unit)

  def kick(reason: String)

  def getDescriptionComponent: BaseComponent

  def addInstantPotionEffect(effect: Potion)
  def addInstantPotionEffect(effect: Potion, level: Int)
  def addPotionEffect(effect: Potion, seconds: Int)
  def addPotionEffect(effect: Potion, seconds: Int, level: Int)
  def addPotionEffect(effect: Potion, seconds: Int, level: Int, ambient: Boolean)
  def addInfinitePotionEffect(effect: Potion)
  def addInfinitePotionEffect(effect: Potion, level: Int)
  def addInfinitePotionEffect(effect: Potion, level: Int, ambient: Boolean)
  def clearPotionEffects()
  def clearPotionEffect(effect: Potion)
}
