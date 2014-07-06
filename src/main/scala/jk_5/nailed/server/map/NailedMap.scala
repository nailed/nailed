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
  override def getWorlds: Array[World] = NailedMapLoader.getWorldsForMap(this) match {
    case Some(s) => s.toArray
    case None => new Array[World](0)
  }
  override def getMappack = this.mappack

  def addWorld(world: World){
    NailedMapLoader.addWorldToMap(world, this)
  }

  override def toString = s"NailedMap{id=$id,mappack=$mappack}"
}
