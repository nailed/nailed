package jk_5.nailed.api.map

/**
 * No description given
 *
 * @author jk-5
 */
trait GameManager {

  def startGame(): Boolean
  def endGame(): Boolean
  def isGameRunning: Boolean

  def hasCustomGameType: Boolean
  def getGameType: GameType
}
