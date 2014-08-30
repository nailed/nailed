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

import io.netty.util.concurrent.{Future, FutureListener}
import jk_5.nailed.api.Server
import jk_5.nailed.api.chat._
import jk_5.nailed.api.command._
import jk_5.nailed.api.map.Map

/**
 * No description given
 *
 * @author jk-5
 */
object CommandLoadmap extends Command("loadmap") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]) = args.length match {
    case 1 =>
      val mappack = Server.getInstance.getMappackRegistry.getByName(args(0))
      if(mappack.isEmpty){
        sender.sendMessage(new ComponentBuilder("Unknown mappack " + args(0)).color(ChatColor.RED).create())
      }else{
        val future = Server.getInstance.getMapLoader.createMapFor(mappack.get)
        future.addListener(new FutureListener[Map] {
          override def operationComplete(future: Future[Map]){
            val builder = new ComponentBuilder("Map ").color(ChatColor.GREEN)
              .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to go to this map")))
              .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/goto " + future.get().worlds(0).getDimensionId)) //TODO: teleport to default world
            builder.append(future.get().mappack.getMetadata.name).append(" was loaded")
            sender.sendMessage(builder.create())
          }
        })
      }
    case _ => sender.sendMessage(new ComponentBuilder("Usage: /loadmap <mappack>").color(ChatColor.RED).create())
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = {
    if(args.length == 1){
      getOptions(args, Server.getInstance.getMappackRegistry.getAllIds)
    }else List()
  }
}
