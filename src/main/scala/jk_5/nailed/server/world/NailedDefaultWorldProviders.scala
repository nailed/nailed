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

import jk_5.nailed.api.world.{DefaultWorldProviders, Dimension, WorldProvider}

/**
 * No description given
 *
 * @author jk-5
 */
object NailedDefaultWorldProviders extends DefaultWorldProviders {
  private sealed trait IdTracked extends WorldProvider {
    private var id: Int = _
    override final def getId = this.id
    override final def setId(id: Int) = this.id = id
    override def getOptions: String = null
  }
  override def getVoidProvider: WorldProvider = new WorldProvider with IdTracked {
    override def getType = "void"
    override def getDimension = Dimension.OVERWORLD
  }
  override def getOverworldProvider: WorldProvider = new WorldProvider with IdTracked {
    override def getType = "overworld"
    override def getDimension = Dimension.OVERWORLD
  }
  override def getNetherProvider: WorldProvider = new WorldProvider with IdTracked {
    override def getType = "nether"
    override def getDimension = Dimension.NETHER
  }
  override def getEndProvider: WorldProvider = new WorldProvider with IdTracked {
    override def getType = "end"
    override def getDimension = Dimension.END
  }
  override def getFlatProvider(pattern: String): WorldProvider = new WorldProvider with IdTracked {
    override def getType = "flat"
    override def getOptions = pattern
    override def getDimension = Dimension.OVERWORLD
  }
}
