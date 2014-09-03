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
import jk_5.nailed.api.util.Potion

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object CommandEffect extends Command("effect") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val players = args.getPlayers(0)
    ctx.setAnalogOutput(players.length)
    val operation = args.getString(1, "operation")
    if(operation == "clear"){
      players.foreach(_.clearPotionEffects())
      if(players.length == 1){
        ctx.success("Cleared all potion effects of " + players(0).getName)
      }else{
        ctx.success("Cleared all potion effects of " + players.length + " players")
      }
    }else{
      var effect = Potion.byName(operation)
      if(effect == null){
        val id = args.getInt(1, 0)
        effect = Potion.byId(id)
        if(effect == null) throw ctx.error("Unknown potion id " + id)
      }

      if(effect.isInstant){
        val level = args.getInt(2, 1, 256, 1)
        players.foreach(_.addInstantPotionEffect(effect, level))
        ctx.success(s"Added ${effect.getName} level $level to ${players.length} player" + (if(players.length != 1) "s" else ""))
      }else{
        val length = if(args.getString(2, "duration", "") == "infinite") 1000000 else args.getInt(2, 0, 1000000)
        val level = args.getInt(3, 1, 256, 1)
        if(length == 0){
          players.foreach(_.clearPotionEffect(effect))
          ctx.success(s"Removed ${effect.getName} from ${players.length} player" + (if(players.length != 1) "s" else ""))
        }else{
          val infinite = length == 1000000
          players.foreach(_.addPotionEffect(effect, length, level))
          if(!infinite){
            ctx.success(s"Added ${effect.getName} level $level to ${players.length} player${if(players.length != 1) "s" else ""} for $length seconds")
          }else{
            ctx.success(s"Added infinite ${effect.getName} level $level to ${players.length} player${if(players.length != 1) "s" else ""}")
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
