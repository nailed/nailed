package jk_5.nailed.server.team

import jk_5.nailed.api.mappack.MappackTeam
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.team.Team
import jk_5.nailed.server.map.TeamManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedTeam(val mappackTeam: MappackTeam, val manager: TeamManager) extends Team {

  private val memberSet = mutable.HashSet[Player]()

  override val id = mappackTeam.id
  override val color = mappackTeam.color
  override val name = mappackTeam.name

  val scoreboardTeam = manager.getScoreboardManager.getOrCreateTeam(this.id)
  scoreboardTeam.setPrefix(color.toString)
  scoreboardTeam.setDisplayName(name)

  override def members = this.memberSet.toArray

  override def onPlayerJoined(player: Player){
    memberSet += player
    scoreboardTeam.addPlayer(player)
  }

  override def onPlayerLeft(player: Player){
    memberSet -= player
    scoreboardTeam.removePlayer(player)
  }
}
