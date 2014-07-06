package jk_5.nailed.api.chat

import jk_5.nailed.api.command.CommandSender

/**
 * No description given
 *
 * @author jk-5
 */
trait TabExecutor {
  def onTabComplete(sender: CommandSender, args: Array[String]): List[String]
}
