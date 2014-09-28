package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandExperience extends Command("xp") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val amountStr = args.getString(0, "amount")
    val isLevel = amountStr.endsWith("l") || amountStr.endsWith("L")
    val amount = parseInt(ctx, if(isLevel && amountStr.length > 1) amountStr.substring(0, amountStr.length - 1) else amountStr)
    val target = args.getPlayers(1)
    ctx.setAnalogOutput(target.length)

    if(amount == 0) throw ctx.error("Operation does not give or take any experience")

    if(isLevel) {
      target.foreach(p => p.addExperienceLevel(amount))
      ctx.success((if(amount > 0) "Gave" else "Took") + s" ${Math.abs(amount)} levels ${if(amount > 0) "to" else "from"} ${target.length} player${if(target.length == 1) "" else "s"}")
    }else{
      target.foreach(p => p.addExperience(amount))
      ctx.success((if(amount > 0) "Gave" else "Took") + s" ${Math.abs(amount)} experience points ${if(amount > 0) "to" else "from"} ${target.length} player${if(target.length == 1) "" else "s"}")
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 2 => autocompleteUsername(args)
    case _ => List()
  }
}
