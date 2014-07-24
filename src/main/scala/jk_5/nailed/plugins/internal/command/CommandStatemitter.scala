package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.utils.NBTUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

/**
 * No description given
 *
 * @author jk-5
 */
object CommandStatemitter extends Command("statemitter") {

  override def execute(sender: CommandSender, args: Array[String]) = sender match {
    case p: NailedPlayer =>
      val is = new ItemStack(Blocks.command_block, 1)
      NBTUtils.getItemNBT(is).setBoolean("IsStatemitter", true)
      NBTUtils.setDisplayName(is, ChatColor.RESET + "Stat Emitter")
      if(args.length == 1){
        NBTUtils.getItemNBT(is).setString("Content", args(0))
        NBTUtils.addLore(is, args(0))
      }
      p.getEntity.inventory.addItemStackToInventory(is)
    case _ =>
  }
}
