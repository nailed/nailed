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

import jk_5.nailed.api.chat._
import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGamerule extends Command("gamerule") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case s: WorldCommandSender =>
        val gameRules = s.getWorld.getGameRules
        args.length match {
          case 0 =>
            val builder = new ComponentBuilder("Available gamerules: ").color(ChatColor.GREEN)
            var first = true
            for (rule <- gameRules.list) {
              if (!first) builder.append(", ").event(null: HoverEvent) else first = false
              builder.append(rule).color(ChatColor.RESET).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(rule + " = " + gameRules(rule))))
            }
            sender.sendMessage(builder.create())
          case 1 =>
            if(!gameRules.ruleExists(args(0))){
              sender.sendMessage(new ComponentBuilder(s"Gamerule '${args(0)}' does not exist").color(ChatColor.RED).create())
            }else{
              sender.sendMessage(new TextComponent(args(0) + " = " + gameRules(args(0))))
            }
          case 2 =>
            if(!gameRules.ruleExists(args(0))){
              sender.sendMessage(new ComponentBuilder(s"Gamerule '${args(0)}' does not exist").color(ChatColor.RED).create())
            }else{
              gameRules(args(0)) = args(1)
              sender.sendMessage(new ComponentBuilder("Gamerule " + args(0) + " changed to " + args(1)).color(ChatColor.GREEN).create())
            }
          case _ => sender.sendMessage(new ComponentBuilder(s"Usage: /gamerule <name> [value]").color(ChatColor.RED).create())
        }
      case _ => sender.sendMessage(new ComponentBuilder(s"You can't change gamerules, because you are not in a world").color(ChatColor.RED).create())
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = sender match {
    case s: WorldCommandSender =>
      args.length match {
        case 1 => autocomplete(args, s.getWorld.getGameRules.list)
        case 2 => autocomplete(args, "true", "false")
        case _ => List()
      }
    case _ => List()
  }
}
