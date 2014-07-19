package jk_5.nailed.api.mappack.gamerule

import com.google.gson.JsonObject

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
case class ImmutableJsonGameRules(obj: JsonObject, parent: GameRules = null) extends GameRules {

  val rules = mutable.HashMap[String, String]()

  for(o <- obj.entrySet()){
    rules.put(o.getKey, o.getValue.getAsString)
  }

  override def apply(key: String): String = this.rules.getOrElse(key, this.parent(key))
  override def ruleExists(key: String) = this.rules.contains(key) || parent.ruleExists(key)
  override def list: Seq[String] = {
    val ret = mutable.HashSet[String]()
    for(r <- this.rules) ret += r._1
    for(r <- this.parent.list) ret += r
    ret.toSeq
  }

  override def toString = s"ImmutableJsonGameRules{parent=${parent.toString}}"
}
