package jk_5.nailed.api.command

import jk_5.nailed.api.chat.BaseComponent

/**
 * No description given
 *
 * @author jk-5
 */
trait CommandSender {

  /**
   * Get the unique name of this command sender.
   *
   * @return the senders username
   */
  def getName: String

  /**
   * Send a message to this sender.
   *
   * @param message the message to send
   */
  def sendMessage(message: BaseComponent)

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  def sendMessage(messages: BaseComponent*)

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  def sendMessage(messages: Array[BaseComponent])

  /**
   * Checks if this user has the specified permission node.
   *
   * @param permission the node to check
   * @return whether they have this node
   */
  def hasPermission(permission: String): Boolean
}
