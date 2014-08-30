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

package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.command._
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
