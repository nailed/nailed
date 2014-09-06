package jk_5.nailed.api.messaging

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.util.Checks

/**
 * Contains information about a {@link Plugin}s registration to a plugin
 * channel.
 *
 * @author jk-5
 */
final class PluginMessageListenerRegistration(
  private val messenger: Messenger,
  private val plugin: Plugin,
  private val channel: String,
  private val listener: PluginMessageListener
){
  Checks.notNull(messenger, "Messenger cannot be null!")
  Checks.notNull(plugin, "Plugin cannot be null!")
  Checks.notNull(channel, "Channel cannot be null!")
  Checks.notNull(listener, "Listener cannot be null!")

  /**
   * Gets the plugin channel that this registration is about.
   *
   * @return Plugin channel.
   */
  def getChannel = this.channel

  /**
   * Gets the registered listener described by this registration.
   *
   * @return Registered listener.
   */
  def getListener = this.listener

  /**
   * Gets the plugin that this registration is for.
   *
   * @return Registered plugin.
   */
  def getPlugin = this.plugin

  /**
   * Checks if this registration is still valid.
   *
   * @return True if this registration is still valid, otherwise false.
   */
  def isValid = messenger.isRegistrationValid(this)

  override def equals(other: Any): Boolean = other match {
    case that: PluginMessageListenerRegistration =>
      messenger == that.messenger &&
        plugin == that.plugin &&
        channel == that.channel &&
        listener == that.listener
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(messenger, plugin, channel, listener)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
