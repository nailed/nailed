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

package jk_5.nailed.api.scoreboard

import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
trait ScoreboardTeam {

  def id: String
  def displayName: String
  def setDisplayName(displayName: String)
  def prefix: String
  def setPrefix(prefix: String)
  def suffix: String
  def setSuffix(suffix: String)
  def isFriendlyFire: Boolean
  def setFriendlyFire(friendlyFire: Boolean)
  def isFriendlyInvisiblesVisible: Boolean
  def setFriendlyInvisiblesVisible(friendlyInvisiblesVisible: Boolean)

  def addPlayer(player: Player): Boolean
  def removePlayer(player: Player): Boolean

  def getPlayers: Array[Player]
  def getPlayerNames: Array[String]
}
