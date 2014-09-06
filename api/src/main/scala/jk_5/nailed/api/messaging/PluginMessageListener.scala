package jk_5.nailed.api.messaging

import jk_5.nailed.api.player.Player

/**
 * A listener for a specific Plugin Channel, which will receive notifications
 * of messages sent from a client.
 *
 * @author jk-5
 */
trait PluginMessageListener {

  /**
   * A method that will be thrown when a PluginMessageSource sends a plugin
   * message on a registered channel.
   *
   * @param channel Channel that the message was sent through.
   * @param player Source of the message.
   * @param message The raw message that was sent.
   */
  def onPluginMessageReceived(channel: String, player: Player, message: Array[Byte])
}
