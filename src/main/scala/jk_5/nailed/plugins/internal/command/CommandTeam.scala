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
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTeam extends Command("team") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]): Unit = sender match {
    case p: Player => if(args.length > 0) caseInsensitiveMatch(args(0)){
      case "join" =>
        if(args.length == 1){
          throw new CommandUsageException("/team join <username> <team>")
        }else{
          val players = getPlayers(sender, args(1))
          if(args.length == 2){
            throw new CommandUsageException(s"/team join ${args(1)} <team>")
          }else{
            val map = p.getMap
            map.getTeam(args(2)) match {
              case Some(team) =>
                if(args.length == 3){
                  for(p <- players){
                    map.setPlayerTeam(p, team)
                    val msg = new ComponentBuilder(s"Player ").color(ChatColor.GREEN).append(p.getName).append(" is now in team ").append(team.name).color(team.color).create()
                    map.broadcastChatMessage(msg)
                  }
                }else throw new CommandUsageException("/team join <username> <team>")
              case None => throw new CommandException("Unknown team")
            }
          }
        }
      case _ => throw new CommandUsageException("/team join <username> <team>")
    }
    case _ => throw new NotAPlayerException
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
