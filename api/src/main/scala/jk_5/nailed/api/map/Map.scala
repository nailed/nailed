package jk_5.nailed.api.map

import jk_5.nailed.api.mappack.Mappack
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
}
