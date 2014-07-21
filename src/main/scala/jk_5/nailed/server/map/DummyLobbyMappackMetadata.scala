package jk_5.nailed.server.map

import jk_5.nailed.api.mappack.implementation.DefaultMappackWorldProperties
import jk_5.nailed.api.mappack.{MappackAuthor, MappackMetadata, MappackTeam, MappackWorld}

/**
 * No description given
 *
 * @author jk-5
 */
object DummyLobbyMappackMetadata extends MappackMetadata {
  override val name = "Lobby"
  override val worlds = Array[MappackWorld](DefaultMappackWorldProperties)
  override val version = "1.0.0"
  override val authors = new Array[MappackAuthor](0)
  override val teams = new Array[MappackTeam](0)
}
