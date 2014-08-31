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

package jk_5.nailed.api.mappack.implementation

import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.DefaultGameRules
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.Difficulty

/**
 * No description given
 *
 * @author jk-5
 */
object DefaultMappackWorldProperties extends MappackWorld {

  override val name = null
  override val generator = "void"
  override val spawnPoint = new Location(null, 0, 64, 0, 0, 0)
  override val dimension = 0
  override val gameRules = DefaultGameRules
  override val resourcepack = ""
  override val difficulty = Difficulty.PEACEFUL
  override val disableFood = true
}
