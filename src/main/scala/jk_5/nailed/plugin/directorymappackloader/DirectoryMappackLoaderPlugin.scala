package jk_5.nailed.plugin.directorymappackloader

import java.io.File

import jk_5.nailed.api.plugin.Plugin

/**
 * No description given
 *
 * @author jk-5
 */
class DirectoryMappackLoaderPlugin extends Plugin {

  final lazy val mappacksDir = new File(this.getServer.getPluginsFolder.getParentFile, "mappacks")
  private var wasLoaded = false

  override def onLoad() = discoverMappacks()

  def discoverMappacks(){
    this.getLogger.info((if(wasLoaded) "Rel" else "L") + "oading directory mappacks...")
    if(!mappacksDir.exists()) mappacksDir.mkdir()
    var i = 0
    if(!wasLoaded){
      wasLoaded = true
      val existing = this.getServer.getMappackRegistry.getByType(classOf[DirectoryMappack])
      for(m <- existing){
        this.getServer.getMappackRegistry.unregister(m)
        i += 1
      }
      this.getLogger.info(s"Unloaded $i DirectoryMappacks")
    }
    i = 0
    for(file <- mappacksDir.listFiles()){
      if(file.isDirectory){
        val mappackInfoFile = new File(file, "mappack.json")
        if(mappackInfoFile.exists() && mappackInfoFile.isFile){
          val m = new DirectoryMappack(file)
          if(this.getServer.getMappackRegistry.register(m)){
            i += 1
            if(m.getId == "lobby" && this.getServer.getMapLoader.getLobbyMappack == null){
              this.getServer.getMapLoader.setLobbyMappack(m)
            }
          }
          i += 1
        }
      }
    }
    this.getLogger.info(s"Registered $i DirectoryMappacks")
  }
}
