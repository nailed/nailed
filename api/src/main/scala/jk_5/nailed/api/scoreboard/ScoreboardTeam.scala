package jk_5.nailed.api.scoreboard

import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
trait ScoreboardTeam {

  def id: String
  def displayName: String
  def setDisplayName(displayName: String)
  def prefix: String
  def setPrefix(prefix: String)
  def suffix: String
  def setSuffix(suffix: String)
  def isFriendlyFire: Boolean
  def setFriendlyFire(friendlyFire: Boolean)
  def isFriendlyInvisiblesVisible: Boolean
  def setFriendlyInvisiblesVisible(friendlyInvisiblesVisible: Boolean)

  def addPlayer(player: Player): Boolean
  def removePlayer(player: Player): Boolean

  def getPlayers: Array[Player]
  def getPlayerNames: Array[String]
}
