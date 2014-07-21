package jk_5.nailed.server.map

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.team.Team
import jk_5.nailed.server.team.NailedTeam

import scala.collection.{immutable, mutable}

/**
 * No description given
 *
 * @author jk-5
 */
trait TeamManager extends Map {

  val playerTeams = mutable.HashMap[Player, Team]()
  val teams = if(this.getMappack != null){
    val teams = for(t <- this.getMappack.getMetadata.teams) yield (t.id, new NailedTeam(t))
    immutable.HashMap[String, NailedTeam](teams: _*)
  }else immutable.HashMap[String, NailedTeam]()

  val defaultTeam = new Team {
    private val memberSet = mutable.HashSet[Player]()

    override val id = TeamManager.this.getId + ":default"
    override val color = ChatColor.WHITE
    override val members = memberSet.toArray
    override val name = "Default Team"

    override def onPlayerJoined(player: Player) = memberSet += player
    override def onPlayerLeft(player: Player) = memberSet -= player
  }

  /*abstract override def onPlayerJoined(player: Player){
    super.onPlayerJoined(player)


  }

  abstract override def onPlayerLeft(player: Player){
    super.onPlayerLeft(player)


  }*/

  override def getTeam(team: String) = teams.get(team)
  override def getPlayerTeam(player: Player): Team = this.playerTeams.getOrElse(player, this.defaultTeam)
  override def setPlayerTeam(player: Player, team: Team) = this.playerTeams.put(player, team)
  override def getTeams: Array[Team] = teams.values.toArray
}
