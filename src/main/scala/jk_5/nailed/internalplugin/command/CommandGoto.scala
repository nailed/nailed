package jk_5.nailed.internalplugin.command

import jk_5.nailed.api.Server
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGoto extends Command("goto") {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case p: Player =>
        p.teleportTo(Server.getInstance.getWorld(Integer.parseInt(args(0))))
      case _ =>
    }
  }
}
