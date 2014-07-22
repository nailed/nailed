package jk_5.nailed.server.scoreboard

import java.util

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.scoreboard.ScoreboardTeam
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

  def setSuffix(suffix: String){
    Checks.notNull(suffix, "suffix may not be null")
    this.suffix = suffix
    this.update()
  }
  def setPrefix(prefix: String){
    Checks.notNull(prefix, "prefix may not be null")
    this.prefix = prefix
    this.update()
  }
  def setDisplayName(displayName: String){
    Checks.notNull(displayName, "displayName may not be null")
    this.displayName = displayName
    this.update()
  }
  def setFriendlyFire(friendlyFire: Boolean){
    this.isFriendlyFire = friendlyFire
    this.update()
  }
  def setFriendlyInvisiblesVisible(friendlyInvisiblesVisible: Boolean){
    this.isFriendlyInvisiblesVisible = friendlyInvisiblesVisible
    this.update()
  }

  override def getPlayers = this.players.toArray
  override def getPlayerNames = this.players.map(_.getName).toArray

  override def addPlayer(player: Player) = {
    Checks.notNull(player, "player may not be null")
    if(players.add(player)){
      val packet = new S3EPacketTeams
      packet.field_149320_a = id
      packet.field_149317_e = util.Arrays.asList(getPlayerNames: _*)
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
      packet.field_149317_e = util.Arrays.asList(getPlayerNames: _*)
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
    packet.field_149317_e = util.Arrays.asList(this.getPlayerNames: _*)
    packet.field_149314_f = 2 //Update
    packet.field_149315_g = flags
    manager.sendPacket(packet)
  }
}
