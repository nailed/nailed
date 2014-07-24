package jk_5.nailed.server.utils

import java.util

import jk_5.nailed.api.material.{Material, ItemStack => NItemStack}
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
    val ret = new NItemStack(Material.getMaterial(Item.itemRegistry.getIDForObject(is.getItem)), is.stackSize, is.getItemDamage.toShort)
    val tag = is.getTagCompound
    if(tag != null){
      for(t <- tag.func_150296_c().asInstanceOf[util.Set[String]]){
        tag.getTag(t) match {
          case s: NBTTagString => ret.setTag(t, s.func_150285_a_())
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
    if(is.getDisplayName.isDefined) NBTUtils.setDisplayName(ret, is.getDisplayName.get)
    NBTUtils.addLore(ret, is.getLore: _*)
    val nbt = NBTUtils.getItemNBT(ret)
    for(t <- is.getTags) nbt.setString(t._1, t._2)
    ret
  }
}
