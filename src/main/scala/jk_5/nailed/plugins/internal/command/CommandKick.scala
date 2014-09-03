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

  override def execute(ctx: CommandContext, args: Arguments){
    val target = args.getPlayer(0)
    val reason = args.getSpacedString(1, "reason", "No reason given")

    target.kick("Kicked by " + ctx.getName + ". Reason: " + reason)

    val b = new TextComponent("")
    b.setColor(ChatColor.RED)
    b.addExtra("Player ")
    b.addExtra(target.getDescriptionComponent)
    b.addExtra(" was kicked by ")
    b.addExtra(ctx.getDescriptionComponent)
    Server.getInstance.broadcastMessage(b)
    Server.getInstance.broadcastMessage(new ComponentBuilder("Reason: " + reason).color(ChatColor.RED).create())

    val m = new TextComponent("Successfully kicked player ")
    m.addExtra(target.getDescriptionComponent)
    m.setColor(ChatColor.GREEN)
    ctx.sendMessage(m)
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] =
    if(args.length == 1) autocompleteUsername(args) else List()
}
