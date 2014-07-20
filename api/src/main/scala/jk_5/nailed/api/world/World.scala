package jk_5.nailed.api.world

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.EditableGameRules
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

  /**
   * What kind of type is this world?
   *  -1 for nether
   *   0 for overworld
   *   1 for end
   *
   * Defaults to 0 (overworld)
   *
   * @return the world type
   */
  def getType: Int

  def setMap(map: Map)
  def getMap: Option[Map]

  def getConfig: MappackWorld

  def getGameRules: EditableGameRules

  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)
}
