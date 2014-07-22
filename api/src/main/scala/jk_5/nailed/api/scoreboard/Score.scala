package jk_5.nailed.api.scoreboard

/**
 * No description given
 *
 * @author jk-5
 */
trait Score {

  def name: String
  def value: Int
  def setValue(value: Int)
  def addValue(value: Int)
  def update()

  //Additional scala api
  def +=(value: Int): Unit = this.addValue(value)
  def -=(value: Int): Unit = this.addValue(-value)
}
