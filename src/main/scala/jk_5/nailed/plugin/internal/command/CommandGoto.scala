package jk_5.nailed.plugin.internal.command

import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command.{CommandSender, TabExecutor}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGoto extends Command("goto") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case p: Player => // args.length match {
      //  case 0 => sender.sendMessage(new TextComponent(""))
      //}
        p.teleportTo(Server.getInstance.getWorld(Integer.parseInt(args(0))))
      case _ => sender.sendMessage(new ComponentBuilder("You can't teleport to other worlds because you are not a player").color(ChatColor.RED).create())
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => getOptions(args, "map", "world")
    case _ => List()
  }
}
