package jk_5.nailed.server

import jk_5.eventbus.EventBus
import jk_5.nailed.api.Platform
import jk_5.nailed.server.plugin.NailedPluginManager

/**
 * No description given
 *
 * @author jk-5
 */
object NailedPlatform extends Platform {

  val globalEventBus = new EventBus

  override val getAPIVersion = classOf[Platform].getPackage.getImplementationVersion
  override val getImplementationVersion = this.getClass.getPackage.getImplementationVersion
  override val getImplementationName = "Nailed"

  override def getPluginManager = NailedPluginManager
}
