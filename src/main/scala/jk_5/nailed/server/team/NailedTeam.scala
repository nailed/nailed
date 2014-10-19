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

package jk_5.nailed.server.team

import jk_5.nailed.api.map.Team
import jk_5.nailed.api.mappack.metadata.MappackTeam
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.map.TeamManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedTeam(val mappackTeam: MappackTeam, val manager: TeamManager) extends Team {

  private val memberSet = mutable.HashSet[Player]()
  var spawnpoint: Location = null

  override val id = mappackTeam.id
  override val color = mappackTeam.color
  override val name = mappackTeam.name
  override def getName = name

  val scoreboardTeam = manager.getScoreboardManager.getOrCreateTeam(this.id)
  scoreboardTeam.setPrefix(color.toString)
  scoreboardTeam.setDisplayName(name)

  override def members = java.util.Arrays.asList(this.memberSet.toArray: _*)

  def onPlayerJoined(player: Player){
    memberSet += player
    scoreboardTeam.addPlayer(player)
  }

  def onPlayerLeft(player: Player){
    memberSet -= player
    scoreboardTeam.removePlayer(player)
  }

  override def getSpawnPoint = spawnpoint
  override def setSpawnPoint(spawnpoint: Location) = this.spawnpoint = spawnpoint
}
