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
import jk_5.nailed.api.command.sender.CommandSender

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTeam extends Command("team") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val p = ctx.requirePlayer()
    val map = p.getMap
    args.matchArgument(0, "operation"){
      case "join" =>
        val players = args.getPlayers(1)
        ctx.setAnalogOutput(players.length)
        map.getTeam(args.getString(2, "team name")) match {
          case Some(team) =>
            for(p <- players){
              map.setPlayerTeam(p, team)
              val msg = new ComponentBuilder(s"Player ").color(ChatColor.GREEN).append(p.getName).append(" is now in team ").append(team.name).color(team.color).create()
              map.broadcastChatMessage(msg)
            }
          case None => throw ctx.error("Unknown team")
        }
      case _ => ctx.wrongUsage("/team join <username> <team>")
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "join")
    case 2 => sender match {
      case s: WorldCommandSender => autocompleteUsername(args, s.getWorld.getMap.orNull)
      case s => List()
    }
    case 3 => sender match {
      case s: WorldCommandSender if s.getWorld.getMap.isDefined => autocomplete(args, s.getWorld.getMap.get.getTeams.map(_.id))
      case s => List()
    }
    case _ => List()
  }
}
