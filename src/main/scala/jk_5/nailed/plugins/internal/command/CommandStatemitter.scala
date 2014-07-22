package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}

/**
 * No description given
 *
 * @author jk-5
 */
object CommandStatemitter extends Command("statemitter") {

  override def execute(sender: CommandSender, args: Array[String]) = sender match {
    case p: NailedPlayer =>
      val is = new ItemStack(Blocks.command_block, 1)
      val tag = new NBTTagCompound
      tag.setBoolean("IsStatemitter", true)
      if(args.length == 1){
        tag.setString("Content", args(0))
        val list = new NBTTagList
        val display = new NBTTagCompound
        list.appendTag(new NBTTagString(args(0)))
        display.setTag("Lore", list)
        tag.setTag("display", display)
      }
      is.setTagCompound(tag)
      is.setStackDisplayName(ChatColor.RESET + "Stat Emitter")
      p.getEntity.inventory.addItemStackToInventory(is)
    case _ =>
  }
}
