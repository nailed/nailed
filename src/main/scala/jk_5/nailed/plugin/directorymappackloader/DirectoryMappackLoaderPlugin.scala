package jk_5.nailed.plugin.directorymappackloader

import java.io.File

import jk_5.nailed.api.plugin.Plugin

/**
 * No description given
 *
 * @author jk-5
 */
class DirectoryMappackLoaderPlugin extends Plugin {

  final val mappacksDir = new File(this.getServer.getPluginsFolder.getParentFile, "mappacks")

  override def onLoad(){
    if(!mappacksDir.exists()) mappacksDir.mkdir()
    this.getLogger.info("Loading mappacks...")


  }
}
