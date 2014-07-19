package jk_5.nailed.plugin.internal.command

import jk_5.nailed.api.chat._
import jk_5.nailed.api.command.{CommandSender, WorldCommandSender}
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGamerule extends Command("gamerule") {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case s: WorldCommandSender =>
        val gameRules = s.getWorld.getGameRules
        args.length match {
          case 0 =>
            val builder = new ComponentBuilder("Available gamerules: ").color(ChatColor.green)
            var first = true
            for (rule <- gameRules.list) {
              if (!first) builder.append(", ").event(null: HoverEvent) else first = false
              builder.append(rule).color(ChatColor.reset).event(new HoverEvent(HoverEventAction.SHOW_TEXT, new TextComponent(rule + " = " + gameRules(rule))))
            }
            sender.sendMessage(builder.createFlat())
          case 1 =>
            if(!gameRules.ruleExists(args(0))){
              sender.sendMessage(new ComponentBuilder(s"Gamerule '${args(0)}' does not exist").color(ChatColor.red).createFlat())
            }else{
              sender.sendMessage(new TextComponent(args(0) + " = " + gameRules(args(0))))
            }
          case 2 =>
            if(!gameRules.ruleExists(args(0))){
              sender.sendMessage(new ComponentBuilder(s"Gamerule '${args(0)}' does not exist").color(ChatColor.red).createFlat())
            }else{
              gameRules(args(0)) = args(1)
              sender.sendMessage(new ComponentBuilder("Gamerule " + args(0) + " changed to " + args(1)).color(ChatColor.green).createFlat())
            }
          case _ => sender.sendMessage(new ComponentBuilder(s"Usage: /gamerule <name> [value]").color(ChatColor.red).createFlat())
        }
      case _ => sender.sendMessage(new ComponentBuilder(s"You can't change gamerules, because you are not in a world").color(ChatColor.red).createFlat())
    }
  }
}
