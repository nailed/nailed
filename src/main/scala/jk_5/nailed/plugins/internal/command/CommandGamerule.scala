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

  override def execute(ctx: CommandContext, args: Arguments){
    val world = ctx.requireWorld()
    val gameRules = world.getGameRules
    args.amount match {
      case 0 =>
        val builder = new ComponentBuilder("Available gamerules: ").color(ChatColor.GREEN)
        var first = true
        for(rule <- gameRules.list){
          if (!first) builder.append(", ").event(null: HoverEvent) else first = false
          builder.append(rule).color(ChatColor.RESET).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(rule + " = " + gameRules(rule))))
        }
        ctx.sendMessage(builder.create())
      case 1 =>
        val name = args.getString(0)
        if(!gameRules.ruleExists(name)) throw ctx.error(s"Gamerule '$name' does not exist")
        else ctx.sendMessage(new TextComponent(name + " = " + gameRules(name)))
      case 2 =>
        val name = args.getString(0)
        if(!gameRules.ruleExists(name)) throw ctx.error(s"Gamerule '$name' does not exist")
        else{
          val value = args.getString(1)
          gameRules(name) = value
          ctx.success(s"Gamerule $name changed to $value")
        }
      case _ => throw ctx.wrongUsage(s"/gamerule <name> [value]")
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
