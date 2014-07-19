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
