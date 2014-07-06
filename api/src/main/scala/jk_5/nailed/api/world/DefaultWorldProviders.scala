package jk_5.nailed.api.world

/**
 * No description given
 *
 * @author jk-5
 */
trait DefaultWorldProviders {
  def getVoidProvider: WorldProvider
  def getOverworldProvider: WorldProvider
  def getNetherProvider: WorldProvider
  def getEndProvider: WorldProvider
  def getFlatProvider(pattern: String): WorldProvider
}
