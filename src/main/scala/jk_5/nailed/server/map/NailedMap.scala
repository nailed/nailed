package jk_5.nailed.server.map

import java.io.File

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class NailedMap(private val id: Int, private val mappack: Mappack = null, private val baseDir: File) extends Map {

  override def getId = this.id
  override def getWorlds: Array[World] = NailedMapLoader.getWorldsForMap(this) match {
    case Some(s) => s.toArray
    case None => new Array[World](0)
  }
  override def getMappack = this.mappack

  override def addWorld(world: World){
    NailedMapLoader.addWorldToMap(world, this)
  }

  override def onPlayerJoined(player: Player){
    println("Player " + player.toString + " joined map " + this.toString)
  }

  override def onPlayerLeft(player: Player){
    println("Player " + player.toString + " left map " + this.toString)
  }

  override def toString = s"NailedMap{id=$id,mappack=$mappack}"
}
