package jk_5.nailed.server.utils

import jk_5.nailed.api.material.{Material, ItemStack => NItemStack}
import net.minecraft.item.{Item, ItemStack}

/**
 * No description given
 *
 * @author jk-5
 */
object ItemStackConverter {

  implicit def toNailed(is: ItemStack): NItemStack = {
    val ret = new NItemStack(Material.getMaterial(Item.itemRegistry.getNameForObject(is.getItem)), is.stackSize, is.getItemDamage.toShort)
    val tag = is.getTagCompound
    if(tag != null){
      //TODO: tags
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
    val ret = new ItemStack(Item.itemRegistry.getObject(is.getMaterial.getId).asInstanceOf[Item], is.getAmount, is.getDamage.toInt)
    if(is.getDisplayName.isDefined) NBTUtils.setDisplayName(ret, is.getDisplayName.get)
    NBTUtils.addLore(ret, is.getLore: _*)
    val nbt = NBTUtils.getItemNBT(ret)
    for(t <- is.getTags) nbt.setString(t._1, t._2)
    ret
  }
}
