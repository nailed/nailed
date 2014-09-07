package jk_5.nailed.api.map

/**
 * No description given
 *
 * @author jk-5
 */
trait GameTypeRegistry {

  def registerGameType(gameType: GameType)
  def unregisterGameType(gameType: GameType)
  def getTypes: Array[GameType]
  def getByName(name: String): GameType
}
