package jk_5.nailed.api.world

import net.minecraft.world.IBlockAccess
import jk_5.nailed.api.player.Player
import java.io.File

/**
 * No description given
 *
 * @author mattashii
 */
trait Dimension extends IBlockAccess{
  /**
  * Gets the players currently in that dimension
  *
  * @return the list of players
  */
  def getPlayers: List[Player]

  /**
   * Load the dimension from the given file
   *
   * @param file the file that contains the dimension.
   *
   * @return True if the dimension loaded successfully
   */
  def loadDimension(file: File): Boolean

  /**
   * Unloads the dimension
   *
   * @return True if the dimension was unloaded successfully
   */
  def unloadDimension: Boolean

  /**
   * Saves the dimension to the given file
   *
   * @param file the file the dimension should be saved to
   *
   * @return True if the dimension was saved successfully
   */
  def saveDimension(file: File): Boolean

  /**
   * Get the vanilla dimension id of this dimension.
   *
   * @return the dimension id
   */
  def getDimensionID: Int
}
