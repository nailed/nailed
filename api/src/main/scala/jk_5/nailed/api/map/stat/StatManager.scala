package jk_5.nailed.api.map.stat

/**
 * No description given
 *
 * @author jk-5
 */
trait StatManager {

  def getStat(name: String): Stat
  def fireEvent(event: StatEvent)
}
