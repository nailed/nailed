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

package jk_5.nailed.server.world

import jk_5.nailed.api.Server
import jk_5.nailed.api.world.{World, WorldContext, WorldProvider}

/**
 * No description given
 *
 * @author jk-5
 */
trait WorldProviders extends Server {
  override def getDefaultWorldProviders = NailedDefaultWorldProviders

  override def createNewWorld(provider: WorldProvider, ctx: WorldContext): World = {
    val id = NailedDimensionManager.getNextFreeDimensionId
    NailedDimensionManager.registerDimension(id, provider)
    NailedDimensionManager.initWorld(id, ctx)
    val world = NailedDimensionManager.getWorld(id)
    world
  }
}
