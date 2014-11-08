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

package jk_5.nailed.server.utils

import jk_5.nailed.api.gamerule.GameRuleKey
import jk_5.nailed.api.world.Difficulty
import jk_5.nailed.server.NailedPlatform
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.{ItemFood, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{DamageSource, FoodStats}

/**
 * No description given
 *
 * @author jk-5
 */
class NailedFoodStats extends FoodStats {

  private var foodLevel = 20
  private var saturation = 5F
  private var exhaustion = 0F
  private var timer = 0

  override def addStats(food: Int, saturation: Float) {
    foodLevel = Math.min(food + foodLevel, 20)
    this.saturation = Math.min(this.saturation + food.toFloat * saturation * 2.0F, foodLevel.toFloat)
  }

  override def addStats(foodItem: ItemFood, is: ItemStack) {
    this.addStats(foodItem.getHealAmount(is), foodItem.getSaturationModifier(is))
  }

  override def onUpdate(playerEntity: EntityPlayer){
    val player = NailedPlatform.getPlayerFromEntity(playerEntity.asInstanceOf[EntityPlayerMP])
    val world = player.getWorld

    if(world.getConfig.disableFood){
      foodLevel = 20
      return
    }

    val difficulty = world.getDifficulty

    if(exhaustion > 4){
      exhaustion -= 4
      if(saturation > 0){
        saturation = Math.max(saturation - 1, 0)
      }else if(difficulty != Difficulty.PEACEFUL){
        foodLevel = Math.max(foodLevel - 1, 0)
      }
    }

    if(world.getGameRules.get(GameRuleKey.NATURAL_REGENERATION).getValue == true && foodLevel >= 18 && playerEntity.shouldHeal()){
      timer += 1
      if(timer >= 80){
        player.heal(1)
        addExhaustion(3)
        timer = 0
      }
    }else if(foodLevel <= 0){
      timer += 1
      if(timer >= 80){
        if(player.getHealth > 10 || difficulty == Difficulty.HARD || player.getHealth > 1 && difficulty == Difficulty.NORMAL){
          playerEntity.attackEntityFrom(DamageSource.starve, 1)
        }
        timer = 0
      }
    }else{
      timer = 0
    }
  }

  override def readNBT(tag: NBTTagCompound) {
    if(tag.hasKey("foodLevel", 99)){
      foodLevel = tag.getInteger("foodLevel")
      timer = tag.getInteger("foodTickTimer")
      saturation = tag.getFloat("saturation")
      exhaustion = tag.getFloat("exhaustion")
    }
  }

  override def writeNBT(tag: NBTTagCompound) {
    tag.setInteger("foodLevel", foodLevel)
    tag.setInteger("foodTickTimer", timer)
    tag.setFloat("saturation", saturation)
    tag.setFloat("exhaustion", exhaustion)
  }

  override def addExhaustion(amount: Float){
    this.exhaustion = Math.min(this.exhaustion + amount, 40.0F)
  }

  override def getSaturationLevel = this.saturation
  override def getFoodLevel = this.foodLevel
  override def needFood = this.foodLevel < 20
}
