package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandAnalog extends Command("analog") {

  override def execute(ctx: CommandContext, args: Arguments){
    ctx.setAnalogOutput(args.getInt(0, 0, 15))
  }
}
