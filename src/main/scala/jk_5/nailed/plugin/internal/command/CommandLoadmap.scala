package jk_5.nailed.plugin.internal.command

import io.netty.util.concurrent.{Future, FutureListener}
import jk_5.nailed.api.Server
import jk_5.nailed.api.chat._
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.plugin.Command

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
        sender.sendMessage(new ComponentBuilder("Unknown mappack " + args(0)).color(ChatColor.red).createFlat())
      }else{
        val future = Server.getInstance.getMapLoader.createMapFor(mappack.get)
        future.addListener(new FutureListener[Map] {
          override def operationComplete(future: Future[Map]){
            val builder = new ComponentBuilder("Map ").color(ChatColor.green)
              .event(new HoverEvent(HoverEventAction.SHOW_TEXT, new TextComponent("Click to go to this map")))
              .event(new ClickEvent(ClickEventAction.RUN_COMMAND, "/goto " + future.get().getWorlds(0).getDimensionId)) //TODO: teleport to default world
            builder.append(future.get().getMappack.getMetadata.name).append(" was loaded")
            sender.sendMessage(builder.createFlat())
          }
        })
      }
    case _ => sender.sendMessage(new ComponentBuilder("Usage: /loadmap <mappack>").color(ChatColor.red).createFlat())
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = {
    if(args.length == 1){
      getOptions(args, Server.getInstance.getMappackRegistry.getAllIds)
    }else List()
  }
}
