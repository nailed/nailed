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
          sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
        }else{
          val player = getTargetPlayer(sender, args(1))
          if(player.isEmpty) return
          if(args.length == 2){
            sender.sendMessage(new ComponentBuilder(s"Usage: /team join ${args(1)} <team>").color(ChatColor.RED).create())
          }else{
            val map = p.getMap
            map.getTeam(args(2)) match {
              case Some(team) =>
                if(args.length == 3){
                  map.setPlayerTeam(player.get, team)
                  val msg = new ComponentBuilder(s"Player ").color(ChatColor.GREEN).append(player.get.getName).append(" is now in team ").append(team.name).color(team.color).create()
                  map.broadcastChatMessage(msg)
                }else sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
              case None => sender.sendMessage(new ComponentBuilder("Unknown team").color(ChatColor.RED).create())
            }
          }
        }
      case _ => sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
    }
    case _ => sender.sendMessage(new ComponentBuilder("You are not a player").color(ChatColor.RED).create())
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => getOptions(args, "join")
    case 2 => sender match {
      case s: WorldCommandSender => getUsernameOptions(args, s.getWorld.getMap.orNull)
      case s => List()
    }
    case 3 => sender match {
      case s: WorldCommandSender if s.getWorld.getMap.isDefined => getOptions(args, s.getWorld.getMap.get.getTeams.map(_.id))
      case s => List()
    }
    case _ => List()
  }
}
