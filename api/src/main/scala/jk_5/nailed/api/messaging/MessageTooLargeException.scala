package jk_5.nailed.api.messaging

/**
 * No description given
 *
 * @author jk-5
 */
class MessageTooLargeException(msg: String) extends RuntimeException(msg) {
  def this() = this("Attempted to send a plugin message that was too large. The maximum length a plugin message may be is " + Messenger.MAX_MESSAGE_SIZE + " bytes.")
  def this(length: Int) = this("Attempted to send a plugin message that was too large. The maximum length a plugin message may be is " + Messenger.MAX_MESSAGE_SIZE + " bytes (tried to send one that is " + length + " bytes long).")
  def this(message: Array[Byte]) = this(message.length)
}
