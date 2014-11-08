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

import java.util

import jk_5.nailed.api.item.{Material, ItemStack => NItemStack}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagString

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object ItemStackConverter {

  implicit def toNailed(is: ItemStack): NItemStack = {
    if(is == null) return null
    val ret = new NItemStack(Material.getMaterial(Item.itemRegistry.getIDForObject(is.getItem)), is.stackSize, is.getMetadata.toShort)
    val tag = is.getTagCompound
    if(tag != null){
      for(t <- tag.getKeySet.asInstanceOf[util.Set[String]]){
        tag.getTag(t) match {
          case s: NBTTagString => ret.setTag(t, s.getString)
          case _ =>
        }
      }
      if(tag.hasKey("display")){
        val disp = tag.getCompoundTag("display")
        if(disp.hasKey("Name")){
          ret.setDisplayName(disp.getString("Name"))
        }
        if(disp.hasKey("Lore")){
          val list = disp.getTagList("Lore", 8)
          for(i <- 0 until list.tagCount()) ret.addLore(list.getStringTagAt(i))
        }
      }
    }
    ret
  }

  implicit def toVanilla(is: NItemStack): ItemStack = {
    if(is == null) return null
    val ret = new ItemStack(Item.itemRegistry.getObjectById(is.getMaterial.getLegacyId).asInstanceOf[Item], is.getAmount, is.getDamage.toInt)
    if(is.getDisplayName != null) NBTUtils.setDisplayName(ret, is.getDisplayName)
    NBTUtils.addLore(ret, is.getLore: _*)
    val nbt = NBTUtils.getItemNBT(ret)
    for(t <- is.getTags) nbt.setString(t._1, t._2)
    ret
  }
}
