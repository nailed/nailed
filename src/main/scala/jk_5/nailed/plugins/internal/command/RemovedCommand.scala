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

import jk_5.nailed.api.chat.{ChatColor, TextComponent}
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
class RemovedCommand(name: String) extends Command(name) {

  override def execute(sender: CommandSender, args: Array[String]){
    //val c = new TranslatableComponent("commands.generic.notFound")
    val c = new TextComponent("Unknown command. Try /help for a list of commands")
    c.setColor(ChatColor.RED)
    sender.sendMessage(c)
  }
}