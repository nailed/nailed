package jk_5.nailed.api.world

/**
 * No description given
 *
 * @author jk-5
 */
trait WorldProvider {

  def setId(id: Int)
  def getId: Int

  //TODO: this is temporary
  def getType: String
  def getOptions: String
}
