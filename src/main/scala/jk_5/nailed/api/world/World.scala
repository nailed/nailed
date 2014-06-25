package jk_5.nailed.api.world

import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author mattashii
 */
trait World {
  /**
   * Get the unique name of the map.
   *
   * @return the worlds name
   */
  def getName: String

  /**
   * Get the players in the map.
   *
   * @return the player list
   */
  def getPlayers: List[Player]

  /**
   * Get the list of dimensions the world contains.
   *
   * @return a list of dimensions
   */
  def getDimensions: List[Dimension]
}
