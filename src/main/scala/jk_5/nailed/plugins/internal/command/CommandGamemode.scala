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
import jk_5.nailed.api.player.{GameMode, Player}

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGamemode extends Command("gamemode", "gm") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    if(args.length == 0) sender match {
      case p: Player => toggleGamemode(p)
      case _ => sender.sendMessage(new ComponentBuilder("You are not a player").color(ChatColor.RED).create())
    }else if(args.length == 1) sender match {
      case p: Player =>
        val mode = fromString(args(0))
        if(mode.isEmpty){
          sender.sendMessage(new ComponentBuilder("Unknown gamemode").color(ChatColor.RED).create())
        }else{
          p.setGameMode(mode.get)
        }
      case _ => sender.sendMessage(new ComponentBuilder("You are not a player").color(ChatColor.RED).create())
    }else if(args.length == 2){
      val players = getTargetPlayer(sender, args(1)) //TODO: player selector
      val newmode = fromString(args(0))
      if(newmode.isEmpty){
        sender.sendMessage(new ComponentBuilder("Unknown gamemode").color(ChatColor.RED).create())
      }else{
        players.get.setGameMode(newmode.get) //TODO: multiple players with the selector
      }
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "survival", "creative", "adventure")
    case 2 => autocompleteUsername(args)
    case _ => List()
  }

  def fromString(s: String) =
    if(s.equalsIgnoreCase("survival") || s.equalsIgnoreCase("s") || s == "0") Some(GameMode.SURVIVAL)
    else if(s.equalsIgnoreCase("creative") || s.equalsIgnoreCase("c") || s == "1") Some(GameMode.CREATIVE)
    else if(s.equalsIgnoreCase("adventure") || s.equalsIgnoreCase("a") || s == "2") Some(GameMode.ADVENTURE)
    else None

  def toggleGamemode(p: Player) = p.setGameMode(if(p.getGameMode == GameMode.SURVIVAL) GameMode.CREATIVE else GameMode.SURVIVAL)
}
