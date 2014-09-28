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
import jk_5.nailed.api.world.WeatherType

/**
 * No description given
 *
 * @author jk-5
 */
object CommandWeather extends Command("weather") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val world = ctx.requireWorld()
    if(args.amount == 0){
      ctx.success(s"Current weather in world ${world.getName}: ${world.getWeather.name().toLowerCase}")
      return
    }
    args.matchArgument(0, "operation") {
      case "clear" =>
        world.setWeather(WeatherType.DRY)
        ctx.success("Weather changed to dry")
      case "rain" =>
        world.setWeather(WeatherType.RAIN)
        ctx.success("Weather changed to raining")
      case "thunder" =>
        world.setWeather(WeatherType.THUNDER)
        ctx.success("Weather changed to thundering")
      case w => throw ctx.error(s"Unknown weather type $w")
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "clear", "rain", "thunder")
    case _ => List()
  }
}
