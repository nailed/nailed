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

package jk_5.nailed.server.scoreboard

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.scoreboard.{Objective, Score}
import jk_5.nailed.api.util.Checks
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.network.play.server.{S3BPacketScoreboardObjective, S3CPacketUpdateScore}

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
case class NailedObjective(id: String, manager: NetworkedScoreboardManager) extends Objective {

  val scores = mutable.HashSet[Score]()
  val scoresByName = mutable.HashMap[String, Score]()

  var displayName: String = id

  override def setDisplayName(displayName: String){
    Checks.notNull(displayName, "displayName may not be null")
    Checks.check(displayName.length() <= 32, "displayName may not be longer than 32")

    this.displayName = displayName
    val packet = new S3BPacketScoreboardObjective
    packet.field_149343_a = this.id
    packet.field_149341_b = this.displayName
    packet.field_149342_c = 2 //Update
    this.manager.sendPacket(packet)
  }

  override def score(name: String): Score = {
    Checks.notNull(name, "name may not be null")
    scoresByName.get(name) match {
      case Some(s) => s
      case None =>
        val score = new NailedScore(this, name)
        this.scores += score
        this.scoresByName.put(name, score)
        score
    }
  }

  override def removeScore(score: Score){
    Checks.notNull(score, "score may not be null")
    if(this.scores.remove(score)){
      this.scoresByName.remove(score.name)
      val p = new S3CPacketUpdateScore
      p.name = score.name
      p.action = S3CPacketUpdateScore.Action.REMOVE
      manager.sendPacket(p)
    }
  }

  def sendData(player: Player){
    for(score <- this.scores){
      val p = new S3CPacketUpdateScore
      p.name = score.name
      p.objective = this.id
      p.value = score.value
      p.action = S3CPacketUpdateScore.Action.CHANGE
      player.asInstanceOf[NailedPlayer].sendPacket(p)
    }
  }
}
