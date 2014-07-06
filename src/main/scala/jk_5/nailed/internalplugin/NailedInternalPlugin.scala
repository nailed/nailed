package jk_5.nailed.internalplugin

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.internalplugin.command.CommandGoto

/**
 * No description given
 *
 * @author jk-5
 */
class NailedInternalPlugin extends Plugin {

  override def onLoad(){
    this.getPluginManager.registerCommand(this, CommandGoto)
  }
}
