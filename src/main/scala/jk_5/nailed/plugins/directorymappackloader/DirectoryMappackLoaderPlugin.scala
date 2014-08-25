/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.plugins.directorymappackloader

import java.io.File

import jk_5.nailed.api.mappack.implementation.JsonMappackMetadata
import jk_5.nailed.api.mappack.{MappackConfigurationException, MappackMetadata}
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata

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
    if(wasLoaded){
      val existing = this.getServer.getMappackRegistry.getByType(classOf[DirectoryMappack])
      for(m <- existing){
        this.getServer.getMappackRegistry.unregister(m)
        i += 1
      }
      this.getLogger.info(s"Unloaded $i DirectoryMappacks")
    }else wasLoaded = true
    i = 0
    for(file <- mappacksDir.listFiles()){
      if(file.isDirectory){
        val jsonMappackMetadata = new File(file, "mappack.json")
        val xmlMappackMetadata = new File(file, "game.xml")

        try{
          val metadata: MappackMetadata = if(xmlMappackMetadata.exists() && xmlMappackMetadata.isFile){
            getLogger.trace("Attempting to load xml mappack " + file.getName)
            XmlMappackMetadata.fromFile(xmlMappackMetadata)
          }else if(jsonMappackMetadata.exists() && jsonMappackMetadata.isFile){
            getLogger.trace("Attempting to load json mappack " + file.getName)
            JsonMappackMetadata.fromFile(jsonMappackMetadata)
          }else null

          if(metadata != null){
            val mappack = new DirectoryMappack(file, metadata)
            if(this.getServer.getMappackRegistry.register(mappack)){
              i += 1
              if(mappack.getId == "lobby"){
                this.getServer.getMapLoader.setLobbyMappack(mappack)
              }
            }
          }
        }catch{
          case e: MappackConfigurationException =>
            getLogger.warn("Configuration for mappack " + file.getName + " is invalid: " + e.getMessage)
        }
      }
    }
    this.getLogger.info(s"Registered $i DirectoryMappacks")
  }
}
