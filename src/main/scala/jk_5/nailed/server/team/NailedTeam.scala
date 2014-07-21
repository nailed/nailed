package jk_5.nailed.server.team

import jk_5.nailed.api.mappack.MappackTeam
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.team.Team

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedTeam(val mappackTeam: MappackTeam) extends Team {

  private val memberSet = mutable.HashSet[Player]()

  override val id = mappackTeam.id
  override val color = mappackTeam.color
  override val name = mappackTeam.name

  override def members = this.memberSet.toArray

  override def onPlayerJoined(player: Player){
    memberSet += player
  }

  override def onPlayerLeft(player: Player){
    memberSet -= player
  }
}
