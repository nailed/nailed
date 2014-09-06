package jk_5.nailed.api.messaging

import java.util

import com.google.common.collect.ImmutableSet
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.util.Checks

import scala.collection.convert.wrapAsScala._

/**
 * Standard implementation to `Messenger`
 */
object StandardMessenger {

  /**
   * Validates a Plugin Channel name.
   *
   * @param channel Channel name to validate.
   */
  def validateChannel(channel: String){
    Checks.notNull(channel, "Channel cannot be null")
    if(channel.length > Messenger.MAX_CHANNEL_SIZE){
      throw new ChannelNameTooLongException(channel)
    }
  }

  /**
   * Validates the input of a Plugin Message, ensuring the arguments are all
   * valid.
   *
   * @param messenger Messenger to use for validation.
   * @param source Source plugin of the Message.
   * @param channel Plugin Channel to send the message by.
   * @param message Raw message payload to send.
   * @throws IllegalArgumentException Thrown if the source plugin is
   *                                  disabled.
   * @throws IllegalArgumentException Thrown if source, channel or message
   *                                  is null.
   * @throws MessageTooLargeException Thrown if the message is too big.
   * @throws ChannelNameTooLongException Thrown if the channel name is too
   *                                     long.
   * @throws ChannelNotRegisteredException Thrown if the channel is not
   *                                       registered for this plugin.
   */
  def validatePluginMessage(messenger: Messenger, source: Plugin, channel: String, message: Array[Byte]) {
    Checks.notNull(messenger, "Messenger cannot be null")
    Checks.notNull(source, "Plugin cannot be null")
    Checks.notNull(message, "Message cannot be null")
    if(!messenger.isOutgoingChannelRegistered(source, channel)){
      throw new ChannelNotRegisteredException(channel)
    }
    if(message.length > Messenger.MAX_MESSAGE_SIZE){
      throw new MessageTooLargeException(message)
    }
    validateChannel(channel)
  }
}

class StandardMessenger extends Messenger {

  private final val incomingByChannel = new util.HashMap[String, util.Set[PluginMessageListenerRegistration]]
  private final val incomingByPlugin = new util.HashMap[Plugin, util.Set[PluginMessageListenerRegistration]]
  private final val outgoingByChannel = new util.HashMap[String, util.Set[Plugin]]
  private final val outgoingByPlugin = new util.HashMap[Plugin, util.Set[String]]
  private final val incomingLock = new AnyRef
  private final val outgoingLock = new AnyRef

  private def addToOutgoing(plugin: Plugin, channel: String){
    outgoingLock synchronized {
      var plugins = outgoingByChannel.get(channel)
      var channels = outgoingByPlugin.get(plugin)
      if(plugins == null){
        plugins = new util.HashSet[Plugin]
        outgoingByChannel.put(channel, plugins)
      }
      if(channels == null){
        channels = new util.HashSet[String]
        outgoingByPlugin.put(plugin, channels)
      }
      plugins.add(plugin)
      channels.add(channel)
    }
  }

  private def removeFromOutgoing(plugin: Plugin, channel: String){
    outgoingLock synchronized {
      val plugins = outgoingByChannel.get(channel)
      val channels = outgoingByPlugin.get(plugin)
      if(plugins != null){
        plugins.remove(plugin)
        if(plugins.isEmpty){
          outgoingByChannel.remove(channel)
        }
      }
      if(channels != null){
        channels.remove(channel)
        if(channels.isEmpty){
          outgoingByChannel.remove(channel)
        }
      }
    }
  }

  private def removeFromOutgoing(plugin: Plugin){
    outgoingLock synchronized {
      val channels = outgoingByPlugin.get(plugin)
      if(channels != null){
        val toRemove = channels.toArray(new Array[String](0))
        outgoingByPlugin.remove(plugin)
        for(channel <- toRemove){
          removeFromOutgoing(plugin, channel)
        }
      }
    }
  }

  private def addToIncoming(registration: PluginMessageListenerRegistration){
    incomingLock synchronized {
      var registrations = incomingByChannel.get(registration.getChannel)
      if(registrations == null){
        registrations = new util.HashSet[PluginMessageListenerRegistration]
        incomingByChannel.put(registration.getChannel, registrations)
      }else{
        if(registrations.contains(registration)){
          throw new IllegalArgumentException("This registration already exists")
        }
      }
      registrations.add(registration)
      registrations = incomingByPlugin.get(registration.getPlugin)
      if(registrations == null){
        registrations = new util.HashSet[PluginMessageListenerRegistration]
        incomingByPlugin.put(registration.getPlugin, registrations)
      }else{
        if(registrations.contains(registration)){
          throw new IllegalArgumentException("This registration already exists")
        }
      }
      registrations.add(registration)
    }
  }

  private def removeFromIncoming(registration: PluginMessageListenerRegistration){
    incomingLock synchronized {
      var registrations = incomingByChannel.get(registration.getChannel)
      if(registrations != null){
        registrations.remove(registration)
        if(registrations.isEmpty){
          incomingByChannel.remove(registration.getChannel)
        }
      }
      registrations = incomingByPlugin.get(registration.getPlugin)
      if(registrations != null){
        registrations.remove(registration)
        if(registrations.isEmpty){
          incomingByPlugin.remove(registration.getPlugin)
        }
      }
    }
  }

  private def removeFromIncoming(plugin: Plugin, channel: String){
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        val toRemove = registrations.toArray(new Array[PluginMessageListenerRegistration](0))
        for(registration <- toRemove){
          if(registration.getChannel == channel){
            removeFromIncoming(registration)
          }
        }
      }
    }
  }

  private def removeFromIncoming(plugin: Plugin){
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        val toRemove = registrations.toArray(new Array[PluginMessageListenerRegistration](0))
        incomingByPlugin.remove(plugin)
        for(registration <- toRemove){
          removeFromIncoming(registration)
        }
      }
    }
  }

  def isReservedChannel(channel: String): Boolean = {
    StandardMessenger.validateChannel(channel)
    channel == "REGISTER" || channel == "UNREGISTER"
  }

  def registerOutgoingPluginChannel(plugin: Plugin, channel: String){
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    if(isReservedChannel(channel)){
      throw new ReservedChannelException(channel)
    }
    addToOutgoing(plugin, channel)
  }

  def unregisterOutgoingPluginChannel(plugin: Plugin, channel: String){
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    removeFromOutgoing(plugin, channel)
  }

  def unregisterOutgoingPluginChannel(plugin: Plugin){
    Checks.notNull(plugin, "Plugin cannot be null")
    removeFromOutgoing(plugin)
  }

  def registerIncomingPluginChannel(plugin: Plugin, channel: String, listener: PluginMessageListener): PluginMessageListenerRegistration = {
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    if(isReservedChannel(channel)){
      throw new ReservedChannelException(channel)
    }
    Checks.notNull(listener, "Listener cannot be null")
    val result = new PluginMessageListenerRegistration(this, plugin, channel, listener)
    addToIncoming(result)
    result
  }

  def unregisterIncomingPluginChannel(plugin: Plugin, channel: String, listener: PluginMessageListener){
    Checks.notNull(plugin, "Plugin cannot be null")
    Checks.notNull(listener, "Listener cannot be null")
    StandardMessenger.validateChannel(channel)
    removeFromIncoming(new PluginMessageListenerRegistration(this, plugin, channel, listener))
  }

  def unregisterIncomingPluginChannel(plugin: Plugin, channel: String){
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    removeFromIncoming(plugin, channel)
  }

  def unregisterIncomingPluginChannel(plugin: Plugin){
    Checks.notNull(plugin, "Plugin cannot be null")
    removeFromIncoming(plugin)
  }

  def getOutgoingChannels: util.Set[String] = {
    outgoingLock synchronized {
      val keys = outgoingByChannel.keySet
      return ImmutableSet.copyOf(keys: java.lang.Iterable[String])
    }
  }

  def getOutgoingChannels(plugin: Plugin): util.Set[String] = {
    Checks.notNull(plugin, "Plugin cannot be null")
    outgoingLock synchronized {
      val channels = outgoingByPlugin.get(plugin)
      if(channels != null){
        return ImmutableSet.copyOf(channels: java.lang.Iterable[String])
      }else{
        return ImmutableSet.of()
      }
    }
  }

  def getIncomingChannels: util.Set[String] = {
    incomingLock synchronized {
      val keys = incomingByChannel.keySet
      return ImmutableSet.copyOf(keys: java.lang.Iterable[String])
    }
  }

  def getIncomingChannels(plugin: Plugin): util.Set[String] = {
    Checks.notNull(plugin, "Plugin cannot be null")
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        val builder = ImmutableSet.builder[String]()
        for(registration <- registrations){
          builder.add(registration.getChannel)
        }
        return builder.build
      }else{
        return ImmutableSet.of[String]()
      }
    }
  }

  def getIncomingChannelRegistrations(plugin: Plugin): util.Set[PluginMessageListenerRegistration] = {
    Checks.notNull(plugin, "Plugin cannot be null")
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        return ImmutableSet.copyOf(registrations: java.lang.Iterable[PluginMessageListenerRegistration])
      }else{
        return ImmutableSet.of[PluginMessageListenerRegistration]()
      }
    }
  }

  def getIncomingChannelRegistrations(channel: String): util.Set[PluginMessageListenerRegistration] = {
    StandardMessenger.validateChannel(channel)
    incomingLock synchronized {
      val registrations = incomingByChannel.get(channel)
      if(registrations != null){
        return ImmutableSet.copyOf(registrations: java.lang.Iterable[PluginMessageListenerRegistration])
      }else{
        return ImmutableSet.of[PluginMessageListenerRegistration]()
      }
    }
  }

  def getIncomingChannelRegistrations(plugin: Plugin, channel: String): util.Set[PluginMessageListenerRegistration] = {
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        val builder = ImmutableSet.builder[PluginMessageListenerRegistration]()
        for(registration <- registrations){
          if(registration.getChannel == channel){
            builder.add(registration)
          }
        }
        return builder.build()
      }else{
        return ImmutableSet.of[PluginMessageListenerRegistration]()
      }
    }
  }

  def isRegistrationValid(registration: PluginMessageListenerRegistration): Boolean = {
    Checks.notNull(registration, "Registration cannot be null")
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(registration.getPlugin)
      if(registrations != null){
        return registrations.contains(registration)
      }else return false
    }
  }

  def isIncomingChannelRegistered(plugin: Plugin, channel: String): Boolean = {
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    incomingLock synchronized {
      val registrations = incomingByPlugin.get(plugin)
      if(registrations != null){
        for(registration <- registrations){
          if(registration.getChannel == channel){
            return true
          }
        }
      }
      return false
    }
  }

  def isOutgoingChannelRegistered(plugin: Plugin, channel: String): Boolean = {
    Checks.notNull(plugin, "Plugin cannot be null")
    StandardMessenger.validateChannel(channel)
    outgoingLock synchronized {
      val channels = outgoingByPlugin.get(plugin)
      if(channels != null){
        return channels.contains(channel)
      }
      return false
    }
  }

  def dispatchIncomingMessage(source: Player, channel: String, message: Array[Byte]){
    Checks.notNull(source, "Player source cannot be null")
    Checks.notNull(message, "Message cannot be null")
    StandardMessenger.validateChannel(channel)
    val registrations = getIncomingChannelRegistrations(channel)
    for(registration <- registrations){
      registration.getListener.onPluginMessageReceived(channel, source, message)
    }
  }
}
