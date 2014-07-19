package jk_5.nailed.api.mappack.gamerule

/**
 * No description given
 *
 * @author jk-5
 */
trait GameRules {

  def list: Seq[String]
  def apply(key: String): String
  def ruleExists(key: String): Boolean
}
