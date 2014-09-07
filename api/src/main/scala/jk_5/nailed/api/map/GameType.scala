package jk_5.nailed.api.map

/**
 * No description given
 *
 * @author jk-5
 */
trait GameType {

  def getName: String
  def onGameStarted(map: Map)
  def onGameEnded(map: Map)
}
