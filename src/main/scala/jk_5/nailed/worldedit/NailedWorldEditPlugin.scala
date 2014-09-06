package jk_5.nailed.worldedit

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.event.platform.PlatformReadyEvent
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.plugin.logging.Logger

/**
 * No description given
 *
 * @author jk-5
 */
object NailedWorldEditPlugin {
  var logger: Logger = _
  var instance: NailedWorldEditPlugin = _
}

class NailedWorldEditPlugin extends Plugin {

  override def onEnable(){
    this.getDataFolder.mkdir()
    NailedWorldEditPlugin.instance = this
    NailedWorldEditPlugin.logger = this.getLogger

    this.getPluginManager.registerListener(this, this)

    WorldEditConfig.load()

    WorldEditBiomeRegistry.populate()

    val we = WorldEdit.getInstance()
    we.getPlatformManager.register(NailedPlatform)

    WorldEdit.getInstance.getEventBus.post(new PlatformReadyEvent)
  }

  override def onDisable(){
    WorldEdit.getInstance.getPlatformManager.unregister(NailedPlatform)
  }
}
