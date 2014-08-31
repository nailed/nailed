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

package jk_5.nailed.api.mappack

import jk_5.nailed.api.mappack.gamerule.GameRules
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.Difficulty

/**
 * No description given
 *
 * @author jk-5
 */
trait MappackWorld {
  def name: String
  def generator: String
  def dimension: Int
  def spawnPoint: Location
  def gameRules: GameRules
  def resourcepack: String
  def difficulty: Difficulty
  def disableFood: Boolean
  def disableDamage: Boolean
  def disableBlockBreaking: Boolean
  def disableBlockPlacement: Boolean
}
