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
import jk_5.nailed.api.scoreboard.{ScoreboardTeam, Visibility}
import jk_5.nailed.api.util.Checks
import net.minecraft.network.play.server.S3EPacketTeams

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedScoreboardTeam(val id: String, val manager: NetworkedScoreboardManager) extends ScoreboardTeam {

  var displayName: String = id
  var prefix = ""
  var suffix = ""
  var isFriendlyFire = true
  var isFriendlyInvisiblesVisible = false
  val players = mutable.HashSet[Player]()
  var nameTagVisibility = Visibility.ALWAYS
  var deathMessageVisibility = Visibility.ALWAYS

  override def getId = id
  override def areFriendlyInvisiblesInvisible() = isFriendlyInvisiblesVisible
  override def getDisplayName = displayName
  override def getSuffix = suffix
  override def getNameTagVisibility = nameTagVisibility
  override def getDeathMessageVisibility = deathMessageVisibility
  override def getPrefix = prefix

  override def setSuffix(suffix: String){
    Checks.notNull(suffix, "suffix may not be null")
    this.suffix = suffix
    this.update()
  }
  override def setPrefix(prefix: String){
    Checks.notNull(prefix, "prefix may not be null")
    this.prefix = prefix
    this.update()
  }
  override def setDisplayName(displayName: String){
    Checks.notNull(displayName, "displayName may not be null")
    this.displayName = displayName
    this.update()
  }
  override def setFriendlyFire(friendlyFire: Boolean){
    this.isFriendlyFire = friendlyFire
    this.update()
  }
  override def setFriendlyInvisiblesVisible(friendlyInvisiblesVisible: Boolean){
    this.isFriendlyInvisiblesVisible = friendlyInvisiblesVisible
    this.update()
  }
  override def setDeathMessageVisibility(deathMessageVisibility: Visibility){
    this.deathMessageVisibility = deathMessageVisibility
    this.update()
  }
  override def setNameTagVisibility(nameTagVisibility: Visibility){
    this.nameTagVisibility = nameTagVisibility
    this.update()
  }

  override def getPlayers = java.util.Arrays.asList(this.players.toArray: _*)
  override def getPlayerNames = java.util.Arrays.asList(this.players.map(_.getName).toArray: _*)

  override def addPlayer(player: Player) = {
    Checks.notNull(player, "player may not be null")
    if(players.add(player)){
      val packet = new S3EPacketTeams
      packet.field_149320_a = id
      packet.field_149317_e = getPlayerNames
      packet.field_149314_f = 3 //Add Player
      manager.sendPacket(packet)
      true
    }else false
  }

  override def removePlayer(player: Player) = {
    Checks.notNull(player, "player may not be null")
    if(players.remove(player)){
      val packet = new S3EPacketTeams
      packet.field_149320_a = id
      packet.field_149317_e = getPlayerNames
      packet.field_149314_f = 4 //Remove Player
      manager.sendPacket(packet)
      true
    }else false
  }

  def update(){
    var flags = 0
    if(this.isFriendlyFire) flags |= 0x1
    if(this.isFriendlyInvisiblesVisible) flags |= 0x2

    val packet = new S3EPacketTeams
    packet.field_149320_a = this.id
    packet.field_149318_b = this.displayName
    packet.field_149319_c = this.prefix
    packet.field_149316_d = this.suffix
    packet.field_149317_e = getPlayerNames
    packet.field_149314_f = 2 //Update
    packet.field_149315_g = flags
    manager.sendPacket(packet)
  }
}
