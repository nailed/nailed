/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

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
