package jk_5.nailed.testplugin

import jk_5.nailed.api.chat.TextComponent
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
class TestCommand extends Command("test") {

  override def execute(sender: CommandSender, args: Array[String]){
    sender.sendMessage(new TextComponent("HAI"))
  }
}
