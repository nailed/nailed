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

import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}

/**
 * No description given
 *
 * @author jk-5
 */
object NBTUtils {

  def addLore(is: ItemStack, lore: String*){
    val tag = getItemNBT(is)
    val display = getOrCreateTagCompound(tag, "display")

    val loreTag = new NBTTagList
    for(l <- lore) loreTag.appendTag(new NBTTagString(l))
    display.setTag("Lore", loreTag)
  }

  def setDisplayName(is: ItemStack, name: String){
    val tag = getItemNBT(is)
    val display = getOrCreateTagCompound(tag, "display")
    if(display.hasKey("Name")) display.removeTag("Name")
    display.setString("Name", name)
  }

  def getItemNBT(is: ItemStack): NBTTagCompound = {
    if(is.hasTagCompound) is.getTagCompound
    else {
      val tag = new NBTTagCompound
      is.setTagCompound(tag)
      tag
    }
  }

  def getOrCreateTagCompound(base: NBTTagCompound, name: String): NBTTagCompound = {
    if(base.hasKey(name)) base.getCompoundTag(name) else {
      val t = new NBTTagCompound
      base.setTag(name, t)
      t
    }
  }
}
