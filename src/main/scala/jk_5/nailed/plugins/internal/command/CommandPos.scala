package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.TextComponent
import jk_5.nailed.api.command._
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
object CommandPos extends Command("pos") {

  override def execute(sender: CommandSender, args: Array[String]) = sender match {
    case p: Player =>
      val location = p.getLocation
      val ret =
        s"""
          | "x": ${location.getX},
          | "y": ${location.getY},
          | "z": ${location.getZ},
          | "yaw": ${location.getYaw},
          | "pitch": ${location.getPitch}
        """.stripMargin
      sender.sendMessage(new TextComponent(ret))
      println(ret)
    case _ => throw new NotAPlayerException
  }
}
