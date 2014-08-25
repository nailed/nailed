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

package jk_5.nailed.api.map

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.scoreboard.ScoreboardManager
import jk_5.nailed.api.team.Team
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
trait Map {

  def id: Int
  def worlds: Array[World]
  def mappack: Mappack
  def addWorld(world: World)

  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)

  def getTeam(team: String): Option[Team]
  def getPlayerTeam(player: Player): Team
  def setPlayerTeam(player: Player, team: Team)
  def getTeams: Array[Team]

  def broadcastChatMessage(message: BaseComponent)
  def broadcastChatMessage(message: BaseComponent*)
  def broadcastChatMessage(message: Array[BaseComponent])

  def players: Array[Player]

  def getScoreboardManager: ScoreboardManager
}
