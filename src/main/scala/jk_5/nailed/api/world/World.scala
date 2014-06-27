package jk_5.nailed.api.world

import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author mattashii
 */
trait World {

  /**
   * Get the dimensionid of this world. This is the id the world is registered to
   *
   * @return dimensionid of this world
   */
  def getDimensionId: Int

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
}
