package jk_5.nailed.internalplugin

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.internalplugin.command.CommandGoto
import jk_5.nailed.internalplugin.mappack.LobbyMappack

/**
 * No description given
 *
 * @author jk-5
 */
class NailedInternalPlugin extends Plugin {

  override def onLoad(){
    this.getPluginManager.registerCommand(this, CommandGoto)
    this.getServer.getMapLoader.setLobbyMappack(LobbyMappack)
  }
}
