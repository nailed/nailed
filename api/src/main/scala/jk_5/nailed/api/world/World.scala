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

package jk_5.nailed.api.world

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.EditableGameRules
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author mattashii
 */
trait World {

  /**
   * Get the dimensionid of this world. This is the id the world is registered to
   *
   * @return dimensionid of this world
   */
  def getDimensionId: Int

  /**
   * Get the unique name of the map.
   *
   * @return the worlds name
   */
  def getName: String

  /**
   * Get the players in the map.
   *
   * @return the player list
   */
  def getPlayers: List[Player]

  /**
   * What kind of type is this world?
   *  -1 for nether
   *   0 for overworld
   *   1 for end
   *
   * Defaults to 0 (overworld)
   *
   * @return the world type
   */
  def getType: Int

  def setMap(map: Map)
  def getMap: Option[Map]

  def getConfig: MappackWorld

  def getGameRules: EditableGameRules

  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)
}
