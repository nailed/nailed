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
import jk_5.nailed.api.chat.{ChatColor, ClickEvent, ComponentBuilder, TextComponent}
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
object CommandHelp extends Command("help", "?") {

  override def execute(sender: CommandSender, args: Array[String]){
    val commands = Server.getInstance.getPluginManager.getAllCommands
      .filter(c => !c.isInstanceOf[RemovedCommand])
      .sortBy(_.getName)
    val pages = (commands.size - 1) / 7
    val page = if(args.length == 0) 0 else parseInt(sender, args(0), 1, pages + 1) - 1

    //TODO: /help commandName - give help about the command

    sender.sendMessage(new ComponentBuilder("--- Showing help page %d of %d (/help <page>) ---".format(page + 1, pages + 1)).color(ChatColor.DARK_GREEN).create())

    val limit = Math.min((page + 1) * 7, commands.size)
    for(i <- page * 7 until limit){
      val c = commands(i)
      val comp = new TextComponent("/" + c.getName)
      comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + c.getName))
      sender.sendMessage(comp)
    }

    if(page == 0){
      sender.sendMessage(new ComponentBuilder("Tip: Use the <tab> key while typing a command to auto-complete the command or its arguments").color(ChatColor.GREEN).create())
    }
  }
}
