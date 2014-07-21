package jk_5.nailed.plugins.internal

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.plugins.internal.command._

/**
 * No description given
 *
 * @author jk-5
 */
class NailedInternalPlugin extends Plugin {

  override def onEnable(){
    this.getPluginManager.registerCommand(this, CommandGoto)
    this.getPluginManager.registerCommand(this, CommandTps)
    this.getPluginManager.registerCommand(this, CommandGamerule)
    this.getPluginManager.registerCommand(this, CommandLoadmap)
    this.getPluginManager.registerCommand(this, CommandTeam)
  }
}
