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

package jk_5.nailed.server.map

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.team.Team
import jk_5.nailed.api.util.Checks
import jk_5.nailed.server.team.NailedTeam

import scala.collection.{immutable, mutable}

/**
 * No description given
 *
 * @author jk-5
 */
trait TeamManager extends Map {

  val playerTeams = mutable.HashMap[Player, Team]()
  lazy val teams = if(this.mappack != null){
    val teams = for(t <- this.mappack.getMetadata.teams) yield (t.id, new NailedTeam(t, this))
    immutable.HashMap[String, NailedTeam](teams: _*)
  }else immutable.HashMap[String, NailedTeam]()

  def init(){
    this.teams //Initialize it
  }

  val defaultTeam = new Team {
    private val memberSet = mutable.HashSet[Player]()

    override val id = TeamManager.this.id + ":default"
    override val color = ChatColor.WHITE
    override val members = memberSet.toArray
    override val name = "Default Team"

    override def onPlayerJoined(player: Player) = memberSet += player
    override def onPlayerLeft(player: Player) = memberSet -= player
  }

  def playerJoined(player: Player){

  }

  def playerLeft(player: Player){

  }

  override def getTeam(team: String) = teams.get(team)
  override def getPlayerTeam(player: Player): Team = this.playerTeams.getOrElse(player, this.defaultTeam)
  override def setPlayerTeam(player: Player, team: Team){
    Checks.notNull(player, "player may not be null")
    val before = this.playerTeams.get(player)
    if(before.isDefined){
      before.get.onPlayerLeft(player)
    }
    if(team == null || team == defaultTeam){
      this.playerTeams.remove(player)
    }else{
      this.playerTeams.put(player, team)
    }
    if(team != null){
      team.onPlayerJoined(player)
    }
  }
  override def getTeams: Array[Team] = teams.values.toArray
}
