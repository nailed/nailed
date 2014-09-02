package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandExperience extends Command("xp") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]){
    if(args.length == 0) throw new CommandUsageException("/xp <amount> [player]")

    val amountStr = args(0)
    val isLevel = amountStr.endsWith("l") || amountStr.endsWith("L")
    val amount = parseInt(sender, if(isLevel && amountStr.length > 1) amountStr.substring(0, amountStr.length - 1) else amountStr, 0)
    val target = senderOrMatches(sender, args, 1)

    if(amount == 0) throw new CommandException("Operation does not give or take any experience")

    if(isLevel) {
      target.foreach(p => p.addExperienceLevel(amount))
      sender.sendMessage(new ComponentBuilder((if(amount > 0) "Given" else "Taken") + s" ${Math.abs(amount)} levels to ${target.length} player${if(target.length == 1) "" else "s"}").color(ChatColor.GREEN).create())
    }else{
      target.foreach(p => p.addExperience(amount))
      sender.sendMessage(new ComponentBuilder((if(amount > 0) "Given" else "Taken") + s" ${Math.abs(amount)} experience points to ${target.length} player${if(target.length == 1) "" else "s"}").color(ChatColor.GREEN).create())
    }
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 2 => autocompleteUsername(args)
    case _ => List()
  }
}
