package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.TextComponent
import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandPos extends Command("pos") {

  override def execute(ctx: CommandContext, args: Arguments){
    val location = ctx.requireLocation()
    val ret = s"""<location x="${location.getX}" y="${location.getY}" z="${location.getZ}" yaw="${location.getYaw}" pitch="${location.getPitch}/>""""
    ctx.sendMessage(new TextComponent(ret))
    println(ret)
  }
}
