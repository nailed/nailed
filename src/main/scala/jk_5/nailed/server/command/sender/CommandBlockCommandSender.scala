package jk_5.nailed.server.command.sender

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.command.CommandSender

/**
 * No description given
 *
 * @author jk-5
 */
class CommandBlockCommandSender extends CommandSender {

  /**
   * Get the unique name of this command sender.
   *
   * @return the senders username
   */
  override def getName: String = ???

  /**
   * Checks if this user has the specified permission node.
   *
   * @param permission the node to check
   * @return whether they have this node
   */
  override def hasPermission(permission: String): Boolean = ???

  /**
   * Send a message to this sender.
   *
   * @param message the message to send
   */
  override def sendMessage(message: BaseComponent): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: BaseComponent*): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: Array[BaseComponent]): Unit = ???
}
