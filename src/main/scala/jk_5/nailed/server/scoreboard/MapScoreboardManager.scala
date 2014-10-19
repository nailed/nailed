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

import java.util

import com.google.common.collect.ImmutableList
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.scoreboard.{DisplayType, Objective, ScoreboardManager, ScoreboardTeam}
import jk_5.nailed.api.util.Checks
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.server.{S3BPacketScoreboardObjective, S3DPacketDisplayScoreboard, S3EPacketTeams}
import net.minecraft.scoreboard.IScoreObjectiveCriteria

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class MapScoreboardManager(val map: NailedMap) extends ScoreboardManager with NetworkedScoreboardManager {

  private val objectives = mutable.HashSet[NailedObjective]()
  private val objectivesById = mutable.HashMap[String, NailedObjective]()
  private val teams = mutable.HashSet[NailedScoreboardTeam]()
  private val teamsById = mutable.HashMap[String, NailedScoreboardTeam]()
  private val displayLocations = new util.EnumMap[DisplayType, Objective](classOf[DisplayType])

  override def getOrCreateObjective(id: String): Objective = {
    Checks.notNull(id, "Id may not be null")
    Checks.check(id.length <= 16, "Id must not be longer than 16")

    objectivesById.get(id) match {
      case Some(t) => t
      case None =>
        val obj = new NailedObjective(id, this)
        this.objectives += obj
        this.objectivesById.put(id, obj)
        val packet = new S3BPacketScoreboardObjective
        packet.field_149343_a = obj.id
        packet.field_149341_b = obj.displayName
        packet.field_149342_c = 0 //0 = Create
        packet.field_179818_c = IScoreObjectiveCriteria.EnumRenderType.INTEGER //TODO: config option
        this.sendPacket(packet)
        obj
    }
  }

  override def getObjective(id: String): Objective = {
    Checks.notNull(id, "Id may not be null")
    objectivesById.get(id).orNull
  }

  def onPlayerJoined(player: Player){
    val np = player.asInstanceOf[NailedPlayer]
    for(objective <- this.objectives){
      val packet = new S3BPacketScoreboardObjective()
      packet.field_149343_a = objective.id
      packet.field_149341_b = objective.displayName
      packet.field_149342_c = 0 //0 = Create
      packet.field_179818_c = IScoreObjectiveCriteria.EnumRenderType.INTEGER //TODO: config option
      np.sendPacket(packet)

      objective.sendData(player)
    }

    for(e <- this.displayLocations.entrySet){
      val packet = new S3DPacketDisplayScoreboard
      packet.field_149374_a = e.getKey.getId
      packet.field_149373_b = e.getValue.getId
      np.sendPacket(packet)
    }

    for(team <- this.teams){
      var flags = 0
      if(team.isFriendlyFire) flags |= 0x1
      if(team.isFriendlyInvisiblesVisible) flags |= 0x2

      val packet = new S3EPacketTeams
      packet.field_149320_a = team.id
      packet.field_149318_b = team.displayName
      packet.field_149319_c = team.prefix
      packet.field_149316_d = team.suffix
      packet.field_149317_e = util.Arrays.asList(team.getPlayerNames: _*)
      packet.field_149314_f = 0 //Create
      packet.field_149315_g = flags
      np.sendPacket(packet)
    }
  }

  def onPlayerLeft(player: Player){
    Checks.notNull(player, "player may not be null")
    if(player.isOnline){
      val np = player.asInstanceOf[NailedPlayer]
      for(objective <- this.objectives){
        val packet = new S3BPacketScoreboardObjective
        packet.field_149343_a = objective.id
        packet.field_149341_b = objective.displayName
        packet.field_149342_c = 1; //Remove
        np.sendPacket(packet)
      }

      for(team <- this.teams){
        val packet = new S3EPacketTeams
        packet.field_149320_a = team.id
        packet.field_149314_f = 1; //Remove
        np.sendPacket(packet)
      }
    }
  }

  override def setDisplay(display: DisplayType, objective: Objective){
    Checks.notNull(display, "Display type may not be null")
    //if(this.displayLocations.get(type) == objective){
    //    return;
    //}
    val packet = new S3DPacketDisplayScoreboard
    packet.field_149374_a = display.getId
    if(objective == null){
      this.displayLocations.remove(display)
      packet.field_149373_b = ""
    }else{
      this.displayLocations.put(display, objective)
      packet.field_149373_b = objective.getId
    }
    this.sendPacket(packet)
  }

  override def getOrCreateTeam(id: String): ScoreboardTeam = {
    Checks.notNull(id, "Id may not be null")

    teamsById.get(id) match {
      case Some(team) => team
      case None =>
        val ret = new NailedScoreboardTeam(id, this)
        this.teamsById.put(id, ret)
        this.teams += ret

        var flags = 0
        if(ret.isFriendlyFire) flags |= 0x1
        if(ret.isFriendlyInvisiblesVisible) flags |= 0x2

        val packet = new S3EPacketTeams
        packet.field_149320_a = ret.id
        packet.field_149318_b = ret.displayName
        packet.field_149319_c = ret.prefix
        packet.field_149316_d = ret.suffix
        packet.field_149317_e = ImmutableList.of()
        packet.field_149314_f = 0; //Create
        packet.field_149315_g = flags
        this.sendPacket(packet)
        ret
    }
  }

  override def getTeam(id: String): ScoreboardTeam = {
    Checks.notNull(id, "id may not be null")
    this.teamsById.get(id).orNull
  }

  override def sendPacket(packet: Packet){
    map.players.map(_.asInstanceOf[NailedPlayer]).foreach(_.sendPacket(packet))
  }
}
