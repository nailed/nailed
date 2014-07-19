package jk_5.nailed.server.map

import jk_5.nailed.api.mappack.{MappackMetadata, MappackWorld}
import jk_5.nailed.api.util.Location

/**
 * No description given
 *
 * @author jk-5
 */
object DummyLobbyMappackMetadata extends MappackMetadata {
  override val name = "Lobby"
  override val worlds = Array[MappackWorld](new MappackWorld {
    override val generator = "void"
    override val name = "default"
    override val spawnPoint = new Location(null, 0, 64, 0, 0, 0)
    override val dimension = 0
  })
}
