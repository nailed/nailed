package jk_5.nailed.server.map.gamerule

import jk_5.nailed.api.mappack.gamerule.{EditableGameRules, GameRules}

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
case class WrappedEditableGameRules(gameRules: GameRules) extends EditableGameRules {

  val rules = mutable.HashMap[String, String]()

  for(rule <- gameRules.list){
    rules.put(rule, gameRules(rule))
  }

  override def apply(key: String): String = this.rules.getOrElse(key, "")
  override def update(key: String, value: String): Unit = this.rules.put(key, value)
  override def ruleExists(key: String) = this.rules.contains(key)
  override def list: Seq[String] = this.rules.keySet.toSeq

  override def toString = s"WrappedEditableGameRules{wrapped=$gameRules}"
}
