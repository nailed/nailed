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
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGamemode extends Command("gamemode", "gm") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    if(args.amount == 0) toggleGamemode(ctx.requirePlayer())
    else{
      val newmode = fromString(ctx, args.getString(0))
      val t = senderOrMatches(ctx, args.arguments, 1)
      t.foreach(_.setGameMode(newmode))
      t.length
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "survival", "creative", "adventure")
    case 2 => autocompleteUsername(args)
    case _ => List()
  }

  def fromString(ctx: CommandContext, s: String) = caseInsensitiveMatchWithResult(s){
    case "0" | "s" | "survival" => GameMode.SURVIVAL
    case "1" | "c" | "creative" => GameMode.CREATIVE
    case "2" | "a" | "adventure" => GameMode.ADVENTURE
    case _ => throw ctx.error(s"Unknown gamemode '$s'")
  }

  def toggleGamemode(p: Player) = p.setGameMode(if(p.getGameMode == GameMode.SURVIVAL || p.getGameMode == GameMode.ADVENTURE) GameMode.CREATIVE else GameMode.SURVIVAL)
}
