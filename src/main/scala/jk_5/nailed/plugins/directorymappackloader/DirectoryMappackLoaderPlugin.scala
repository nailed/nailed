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

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent
import jk_5.nailed.api.mappack.MappackConfigurationException
import jk_5.nailed.api.mappack.metadata.MappackMetadata
import jk_5.nailed.api.mappack.metadata.json.JsonMappackMetadata
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata
import org.slf4j.LoggerFactory

/**
 * No description given
 *
 * @author jk-5
 */
@Plugin(id = "DirectoryMappackLoader", name = "Directory Mappack Loader")
class DirectoryMappackLoaderPlugin {

  val logger = LoggerFactory.getLogger(this.getClass)
  var wasLoaded = false

  @EventHandler
  def registerMappacks(event: RegisterMappacksEvent){
    val mappacksDir = new File(event.getPlatform.getRuntimeDirectory, "mappacks")
    logger.info((if(wasLoaded) "Rel" else "L") + "oading directory mappacks...")
    if(!mappacksDir.exists()) mappacksDir.mkdir()
    var i = 0
    /*if(wasLoaded){
      val existing = this.getServer.getMappackRegistry.getByType(classOf[DirectoryMappack])
      for(m <- existing){
        this.getServer.getMappackRegistry.unregister(m)
        i += 1
      }
      logger.info(s"Unloaded $i DirectoryMappacks")
    }else wasLoaded = true */
    //TODO: unregister existing
    i = 0
    for(file <- mappacksDir.listFiles()){
      if(file.isDirectory){
        val jsonMappackMetadata = new File(file, "mappack.json")
        val xmlMappackMetadata = new File(file, "game.xml")

        try{
          val metadata: MappackMetadata = if(xmlMappackMetadata.exists() && xmlMappackMetadata.isFile){
            logger.trace("Attempting to load xml mappack " + file.getName)
            XmlMappackMetadata.fromFile(xmlMappackMetadata)
          }else if(jsonMappackMetadata.exists() && jsonMappackMetadata.isFile){
            logger.trace("Attempting to load json mappack " + file.getName)
            JsonMappackMetadata.fromFile(jsonMappackMetadata)
          }else null

          if(metadata != null){
            val mappack = new DirectoryMappack(file, metadata)
            if(event.registerMappack(mappack)){
              i += 1
              if(mappack.getId == "lobby"){
                event.setLobbyMappack(mappack)
              }
            }
          }
        }catch{
          case e: MappackConfigurationException =>
            logger.warn("Configuration for mappack " + file.getName + " is invalid: " + e.getMessage)
        }
      }
    }
    logger.info(s"Registered $i DirectoryMappacks")
  }
}
