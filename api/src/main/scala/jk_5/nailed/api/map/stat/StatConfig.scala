package jk_5.nailed.api.map.stat

/**
 * No description given
 *
 * @author jk-5
 */
trait StatConfig {
  def name: String
  def track: String
  def attributes: java.util.Map[String, String]
}
