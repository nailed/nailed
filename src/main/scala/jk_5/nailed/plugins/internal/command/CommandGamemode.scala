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
      case _ => throw new CommandUsageException("/gamemode <mode> <player>")
    }else if(args.length == 1 || args.length == 2){
      val newmode = fromString(args(0))
      senderOrMatches(sender, args, 1).foreach(_.setGameMode(newmode))
    }else{
      if(sender.isInstanceOf[Player]){
        throw new CommandUsageException("/gamemode [mode] [player]")
      }else{
        throw new CommandUsageException("/gamemode <mode> <player>")
      }
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "survival", "creative", "adventure")
    case 2 => autocompleteUsername(args)
    case _ => List()
  }

  def fromString(s: String) = caseInsensitiveMatchWithResult(s){
    case "0" | "s" | "survival" => GameMode.SURVIVAL
    case "1" | "c" | "creative" => GameMode.CREATIVE
    case "2" | "a" | "adventure" => GameMode.ADVENTURE
    case _ => throw new CommandException(s"Unknown gamemode '$s'")
  }

  def toggleGamemode(p: Player) = p.setGameMode(if(p.getGameMode == GameMode.SURVIVAL) GameMode.CREATIVE else GameMode.SURVIVAL)
}
