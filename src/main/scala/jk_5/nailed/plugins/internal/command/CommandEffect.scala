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
import jk_5.nailed.api.util.Potion

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object CommandEffect extends Command("effect") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    if(args.length <= 1) throw new CommandUsageException("/effect <player> <effect>")
    val players = getPlayers(sender, args(0))
    val operation = args(1)
    if(operation == "clear"){
      players.foreach(_.clearPotionEffects())
      if(players.length == 1){
        sender.sendMessage(new ComponentBuilder("Cleared all potion effects of " + players(0).getName).color(ChatColor.GREEN).create())
      }else{
        sender.sendMessage(new ComponentBuilder("Cleared all potion effects of " + players.length + " players").color(ChatColor.GREEN).create())
      }
    }else{
      var effect = Potion.byName(operation)
      if(effect == null){
        val id = parseInt(sender, operation, 0)
        effect = Potion.byId(id)
        if(effect == null) throw new CommandException("Unknown potion id " + id)
      }

      if(effect.isInstant){
        val level = if(args.length > 2) parseInt(sender, args(2), 1, 256) else 1
        players.foreach(_.addInstantPotionEffect(effect, level))
        sender.sendMessage(new ComponentBuilder(s"Added ${effect.getName} level $level to ${players.length} player" + (if(players.length != 1) "s" else "")).color(ChatColor.GREEN).create())
      }else {
        if(args.length <= 2) throw new CommandUsageException(s"/effect ${args(0)} ${args(1)} <duration> [level]")
        val length = if(args(2).equalsIgnoreCase("infinite")) 1000000 else parseInt(sender, args(2), 0, 1000000)
        val level = if(args.length > 3) parseInt(sender, args(3), 1, 256) else 1
        if(length == 0){
          players.foreach(_.clearPotionEffect(effect))
          sender.sendMessage(new ComponentBuilder(s"Removed ${effect.getName} from ${players.length} player" + (if(players.length != 1) "s" else "")).color(ChatColor.GREEN).create())
        }else{
          val infinite = length == 1000000
          players.foreach(_.addPotionEffect(effect, length, level))
          if(!infinite){
            sender.sendMessage(new ComponentBuilder(s"Added ${effect.getName} level $level to ${players.length} player${if(players.length != 1) "s" else ""} for $length seconds").color(ChatColor.GREEN).create())
          }else{
            sender.sendMessage(new ComponentBuilder(s"Added infinite ${effect.getName} level $level to ${players.length} player${if(players.length != 1) "s" else ""}").color(ChatColor.GREEN).create())
          }
        }
      }
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocompleteUsername(args)
    case 2 =>
      val b = mutable.ArrayBuffer[String]()
      b ++= Potion.getNames
      b += "clear"
      autocomplete(args, b)
    case 3 => autocomplete(args, "infinite")
    case _ => List()
  }
}
