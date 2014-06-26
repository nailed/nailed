package jk_5.nailed.api.world

import jk_5.nailed.api.player.Player
import java.io.File
import java.util.UUID

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

  /**
   * Loads the dimensions the world contains.
   *
   * @return True if it did work, otherwise false
   */
  def loadDimensions: Boolean

  /**
   * Unloads the dimensions the world contains.
   *
   * @return True if it did work, otherwise false
   */
  def unloadDimensions: Boolean

  /**
   * Saves all dimensions the world contains to the savefolder
   *
   * @return the file the dimensions are stored in
   */
  def saveDimensions: File

  /**
   * Stops the world processes and removes the references before it is removed from the server.
   * <p>
   * It removes its tickhandlers, and also saves the dimensions if needed.
   */
  def closeWorld

  /**
   * Get the unique id of this world.
   *
   * @return the UUID of this world
   */
  def getUniqueID: UUID
}
