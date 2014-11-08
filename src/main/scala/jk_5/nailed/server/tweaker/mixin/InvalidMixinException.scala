package jk_5.nailed.server.tweaker.mixin

class InvalidMixinException(msg: String, throwable: Throwable = null) extends RuntimeException(msg, throwable) {
  def this(t: Throwable) = this(null, t)
}
