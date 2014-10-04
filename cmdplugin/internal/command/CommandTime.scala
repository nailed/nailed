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

import jk_5.nailed.api.command._
import jk_5.nailed.api.command.sender.CommandSender

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTime extends Command("time") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val world = ctx.requireWorld()
    if(args.amount == 0){
      ctx.success("Current time in world " + world.getName + ": " + world.getTime)
      return
    }
    args.matchArgument(0, "operation"){
      case "set" => args.matchArgument(1, "time"){
        case "day" => world.setTime(6000)
        case "night" => world.setTime(18000)
        case _ =>
          val r = args.getInt(1, 0, 23999)
          world.setTime(r)
          ctx.success("Set time in world " + world.getName + " to " + r)
      }
      case _ =>
        val number = args.getInt(0)
        val world = NailedServer.getWorld(number)
        if(world != null){
          ctx.success("Current time in world " + world.getName + ": " + world.getTime)
        }else{
          throw ctx.error("Was not able to find the world with id " + number)
        }
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] =
    if(args.length == 1) autocomplete(args, "set")
    else if(args.length == 2) autocomplete(args, "day", "night")
    else null
}
