package jk_5.nailed.api.mappack.gamerule

import scala.collection.immutable

/**
 * No description given
 *
 * @author jk-5
 */
object DefaultGameRules extends GameRules {

  private val defaultRules = immutable.HashMap[String, String](
    "doFireTick" -> "true",
    "mobGriefing" -> "true",
    "keepInventory" -> "false",
    "doMobSpawning" -> "true",
    "doMobLoot" -> "true",
    "doTileDrops" -> "true",
    "commandBlockOutput" -> "true",
    "naturalRegeneration" -> "true",
    "doDaylightCycle" -> "true"
  )

  override def list = this.defaultRules.keySet.toSeq
  override def ruleExists(key: String) = this.defaultRules.contains(key)
  override def apply(key: String): String = this.defaultRules.get(key).orNull

  def populate[T <: EditableGameRules](gameRules: T): T = {
    for(r <- this.defaultRules){
      gameRules(r._1) = r._2
    }
    gameRules
  }
}
