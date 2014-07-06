package jk_5.nailed.plugin.internal

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.plugin.internal.command.{CommandGoto, CommandTps}
import jk_5.nailed.plugin.internal.mappack.LobbyMappack

/**
 * No description given
 *
 * @author jk-5
 */
class NailedInternalPlugin extends Plugin {

  override def onLoad(){
    this.getServer.getMapLoader.setLobbyMappack(LobbyMappack)
  }

  override def onEnable(){
    this.getPluginManager.registerCommand(this, CommandGoto)
    this.getPluginManager.registerCommand(this, CommandTps)
  }
}
