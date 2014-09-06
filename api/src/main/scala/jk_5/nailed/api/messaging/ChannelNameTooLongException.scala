package jk_5.nailed.api.messaging

/**
 * No description given
 *
 * @author jk-5
 */
class ChannelNameTooLongException(channel: String) extends RuntimeException("Attempted to send a Plugin Message to a channel that was too large. The maximum length a channel may be is " + Messenger.MAX_CHANNEL_SIZE + " chars" + (if(channel == null) "." else " attempted " + channel.length() + " - '" + channel + ".")) {
  def this() = this(null)
}
