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

import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder, TextComponent}
import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandKick extends Command("kick") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    if(args.length == 0){
      sender.sendMessage(new ComponentBuilder("Usage: /kick <player> [reason]").color(ChatColor.RED).create())
      return
    }
    val target = Server.getInstance.getPlayerByName(args(0))
    val reason = if(args.length > 1){
      val newArray = new Array[String](args.length - 1)
      System.arraycopy(args, 1, newArray, 0, newArray.length)
      newArray.mkString(" ")
    }else "No reason given"

    if(target.isEmpty){
      sender.sendMessage(new ComponentBuilder("Player " + args(0) + " is not online").color(ChatColor.RED).create())
      return
    }

    target.get.kick("Kicked by " + sender.getName + ". Reason: " + reason)

    val b = new TextComponent("")
    b.setColor(ChatColor.RED)
    b.addExtra("Player ")
    b.addExtra(target.get.getDescriptionComponent)
    b.addExtra(" was kicked by ")
    b.addExtra(sender.getDescriptionComponent)
    Server.getInstance.broadcastMessage(b)
    Server.getInstance.broadcastMessage(new ComponentBuilder("Reason: " + reason).color(ChatColor.RED).create())

    val m = new TextComponent("Successfully kicked player ")
    m.addExtra(target.get.getDescriptionComponent)
    m.setColor(ChatColor.GREEN)
    sender.sendMessage(m)
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] =
    if(args.length == 1) autocompleteUsername(args) else List()
}
