package jk_5.nailed.api.map

import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
trait Map {

  def getId: Int
  def getWorlds: Array[World]
  def getMappack: Mappack
  def addWorld(world: World)

  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)
}
