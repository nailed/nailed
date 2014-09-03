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

import jk_5.nailed.api.Server
import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGoto extends Command("goto") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    ctx.requirePlayer().teleportTo(Server.getInstance.getWorld(args.getInt(0)))
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "map", "world")
    case _ => List()
  }
}
