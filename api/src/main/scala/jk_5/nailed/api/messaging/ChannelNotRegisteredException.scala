package jk_5.nailed.api.messaging

/**
 * No description given
 *
 * @author jk-5
 */
class ChannelNotRegisteredException(channel: String) extends RuntimeException("Attempted to send a plugin message through an unregistered channel" + (if(channel == null) "." else " '" + channel + "'")) {
  def this() = this(null)
}
