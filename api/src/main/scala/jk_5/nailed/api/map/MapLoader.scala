package jk_5.nailed.api.map

import io.netty.util.concurrent.Future
import jk_5.nailed.api.mappack.Mappack

/**
 * No description given
 *
 * @author jk-5
 */
trait MapLoader {

  def setLobbyMappack(mappack: Mappack): Boolean
  def getLobbyMappack: Mappack
  def getLobby: Map
  def getMap(mapId: Int): Option[Map]
  def getOrCreateMap(mapId: Int): Map
  def createMapFor(mappack: Mappack): Future[Map]
}
