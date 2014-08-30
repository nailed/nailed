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

import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command._
import jk_5.nailed.server.NailedServer

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTime extends Command("time") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]): Unit = sender match {
    case c: WorldCommandSender =>
      if(args.length > 0){
        caseInsensitiveMatch(args(0)) {
          case "set" =>
            if(args.length > 1){
              caseInsensitiveMatch(args(1)) {
                case "day" => c.getWorld.setTime(6000)
                case "night" => c.getWorld.setTime(18000)
                case t =>
                  val r = parseInt(sender, args(1), 0, 23999)
                  c.getWorld.setTime(r)
                  sender.sendMessage(new ComponentBuilder("Set time in world " + c.getWorld.getName + " to " + r).color(ChatColor.GREEN).create())
              }
            }else{
              throw new CommandUsageException("/time [set] [day|night|0-23999]")
            }
          case _ =>
            val number = parseInt(sender, args(0))
            val world = NailedServer.getWorld(number)
            if(world != null){
              sender.sendMessage(new ComponentBuilder("Current time in world " + world.getName + ": " + world.getTime).color(ChatColor.GREEN).create())
            }else{
              throw new CommandException("Was not able to find the world with id " + number)
            }
        }
      }else{
        sender.sendMessage(new ComponentBuilder("Current time in world " + c.getWorld.getName + ": " + c.getWorld.getTime).color(ChatColor.GREEN).create())
      }
    case _ => throw new NoWorldException
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] =
    if(args.length == 1) autocomplete(args, "set")
    else if(args.length == 2) autocomplete(args, "day", "night")
    else null
}
