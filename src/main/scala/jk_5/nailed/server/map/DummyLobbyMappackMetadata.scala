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

package jk_5.nailed.server.map

import jk_5.nailed.api.mappack.metadata._
import jk_5.nailed.api.mappack.metadata.impl.DefaultMappackWorld

/**
 * No description given
 *
 * @author jk-5
 */
object DummyLobbyMappackMetadata extends MappackMetadata {
  override val name = "Lobby"
  override val worlds = Array[MappackWorld](DefaultMappackWorld.INSTANCE)
  override val version = "1.0.0"
  override val authors = new Array[MappackAuthor](0)
  override val teams = new Array[MappackTeam](0)
  override val tutorial = new Tutorial{
    override val stages = new Array[TutorialStage](0)
  }
  override val gameType = null
  override val stats = new Array[StatConfig](0)
}
