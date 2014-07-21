package jk_5.nailed.plugins.qrmap

import jk_5.nailed.api.plugin.Plugin

/**
 * No description given
 *
 * @author jk-5
 */
class MapPlugin extends Plugin {

  override def onEnable(){
    this.getPluginManager.registerCommand(this, QRMapCommand)
  }
}
