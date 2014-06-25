package jk_5.nailed.testplugin

import jk_5.nailed.api.plugin.Plugin

/**
 * No description given
 *
 * @author jk-5
 */
class TestPlugin extends Plugin {

  override def onEnable(){
    this.getPluginManager.registerCommand(this, new TestCommand)
  }
}
