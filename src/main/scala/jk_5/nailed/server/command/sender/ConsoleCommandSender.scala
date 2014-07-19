package jk_5.nailed.server.command.sender

import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.api.command.CommandSender
import net.minecraft.server.dedicated.DedicatedServer
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class ConsoleCommandSender(val wrapped: DedicatedServer) extends CommandSender {

  val logger = LogManager.getLogger

  /**
   * Get the unique name of this command sender.
   *
   * @return the senders username
   */
  override def getName = "Server"

  /**
   * Checks if this user has the specified permission node.
   *
   * @param permission the node to check
   * @return whether they have this node
   */
  override def hasPermission(permission: String) = true

  /**
   * Send a message to this sender.
   *
   * @param message the message to send
   */
  override def sendMessage(message: BaseComponent) = logger.info(message.toPlainText)

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: BaseComponent*) = logger.info(new TextComponent(messages: _*).toPlainText)

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: Array[BaseComponent]) = logger.info(new TextComponent(messages: _*).toPlainText)
}
