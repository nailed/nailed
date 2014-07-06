package jk_5.nailed.api.world

/**
 * No description given
 *
 * @author jk-5
 */
trait WorldProvider {

  def setId(id: Int)
  def getId: Int

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
  def getTypeId: Int

  //TODO: this is temporary
  def getType: String
  def getOptions: String
}
