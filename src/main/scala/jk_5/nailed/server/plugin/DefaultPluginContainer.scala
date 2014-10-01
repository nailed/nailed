package jk_5.nailed.server.plugin

import jk_5.eventbus.EventBus
import jk_5.nailed.api.event.plugin.PluginEvent
import jk_5.nailed.api.plugin.{Plugin, PluginContainer, PluginIdentifier}
import jk_5.nailed.server.NailedPlatform

/**
 * No description given
 *
 * @author jk-5
 */
class DefaultPluginContainer(val annotation: Plugin, val instance: Any) extends PluginContainer with PluginIdentifier {

  private val eventBus = new EventBus

  eventBus.register(instance)
  NailedPlatform.globalEventBus.register(instance)

  def fireEvent[T <: PluginEvent](event: T): T = {
    eventBus.post(event)
    event
  }
  def getEventBus = eventBus

  override val getId = annotation.id()
  override val getName = annotation.name()
  override val getVersion = annotation.version()
  override def getInstance() = instance.asInstanceOf[AnyRef]
  override def getIdentifier = this

  override def toString = s"DefaultPluginContainer{id=$getId, name=$getName, version=$getVersion, instance=${getInstance()}"
}
