package jk_5.nailed.server.map

import jk_5.nailed.api.mappack.{DefaultMappackWorldProperties, MappackMetadata, MappackWorld}

/**
 * No description given
 *
 * @author jk-5
 */
object DummyLobbyMappackMetadata extends MappackMetadata {
  override val name = "Lobby"
  override val worlds = Array[MappackWorld](DefaultMappackWorldProperties)
}
