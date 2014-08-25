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

package jk_5.nailed.server.map.gamerule

import java.util

import com.google.common.collect.MapMaker
import jk_5.nailed.api.mappack.gamerule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.GameRules
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object DelegatingGameRules {

  val map = new MapMaker().weakKeys().makeMap[gamerule.GameRules, GameRules]()
  val logger = LogManager.getLogger

  def get(original: gamerule.GameRules): GameRules = {
    if(!map.containsKey(original)){
      val v = new DelegatingGameRules(original)
      map.put(original, v)
      v
    }else{
      map.get(original)
    }
  }
}

class DelegatingGameRules(original: gamerule.GameRules) extends GameRules {

  override def addGameRule(key: String, value: String) = DelegatingGameRules.logger.warn("Tried to add gamerule " + key + " with value " + value + " to immutable gamerules object")
  override def setOrCreateGameRule(key: String, value: String) = DelegatingGameRules.logger.warn("Tried to add gamerule " + key + " with value " + value + " to immutable gamerules object")
  override def getGameRuleStringValue(key: String) = original(key)
  override def getGameRuleBooleanValue(key: String) = original(key) == "true"

  override def writeGameRulesToNBT(): NBTTagCompound = {
    val tag = new NBTTagCompound
    for(rule <- this.original.list){
      tag.setString(rule, original(rule))
    }
    tag
  }

  override def readGameRulesFromNBT(tag: NBTTagCompound) = DelegatingGameRules.logger.warn("Tried to read immutable gamerule object from NBT")
  override def getRules: Array[String] = this.original.list.toArray
  override def hasRule(key: String) = this.original.ruleExists(key)
}

class DelegatingEditableGameRules(private val or: gamerule.EditableGameRules) extends DelegatingGameRules(or) {

  override def addGameRule(key : String, value : String) = or(key) = value
  override def setOrCreateGameRule(key: String, value: String) = or(key) = value
  override def readGameRulesFromNBT(tag: NBTTagCompound){
    val keys = tag.func_150296_c().asInstanceOf[util.Set[String]]
    for(k <- keys){
      this.setOrCreateGameRule(k, tag.getString(k))
    }
  }
}
