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
import jk_5.nailed.api.world.WeatherType

/**
 * No description given
 *
 * @author jk-5
 */
object CommandWeather extends Command("weather") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]): Unit = sender match {
    case c: WorldCommandSender =>
      if(args.length > 0){
        caseInsensitiveMatch(args(0)) {
          case "clear" =>
            c.getWorld.setWeather(WeatherType.DRY)
            sender.sendMessage(new ComponentBuilder("Weather changed to dry").color(ChatColor.GREEN).create())
          case "rain" =>
            c.getWorld.setWeather(WeatherType.RAIN)
            sender.sendMessage(new ComponentBuilder("Weather changed to raining").color(ChatColor.GREEN).create())
          case "thunder" =>
            c.getWorld.setWeather(WeatherType.THUNDER)
            sender.sendMessage(new ComponentBuilder("Weather changed to thundering").color(ChatColor.GREEN).create())
          case w => sender.sendMessage(new ComponentBuilder("Unknown weather type " + w).color(ChatColor.RED).create())
        }
      }else{
        sender.sendMessage(new ComponentBuilder("Current weather in world " + c.getWorld.getName + ": " + c.getWorld.getWeather.name().toLowerCase).color(ChatColor.GREEN).create())
      }
    case _ => throw new NoWorldException
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "clear", "rain", "thunder")
    case _ => List()
  }
}
