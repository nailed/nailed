package jk_5.nailed.api.messaging

/**
 * No description given
 *
 * @author jk-5
 */
class ReservedChannelException(channel: String) extends RuntimeException("Attempted to register for a reserved channel name" + (if(channel == null) "." else " ('" + channel + "')")) {
  def this() = this(null)
}
