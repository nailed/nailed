package jk_5.nailed.server.map

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class NailedMap(private val id: Int, private val mappack: Mappack = null) extends Map {

  override def getId = this.id
  override def getWorlds: Array[World] = new Array[World](0)
  override def getSaveFolderName: String = "map_" + (if(mappack == null) "" else mappack.getId + "_") + id
  override def getMappack = this.mappack

  def addWorld(world: World){

  }
}
