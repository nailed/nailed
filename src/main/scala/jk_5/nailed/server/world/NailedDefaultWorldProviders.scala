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

import jk_5.nailed.api.world.{DefaultWorldProviders, WorldProvider}

/**
 * No description given
 *
 * @author jk-5
 */
object NailedDefaultWorldProviders extends DefaultWorldProviders {
  override def getVoidProvider: WorldProvider = new WorldProvider {
    var id: Int = _
    override def getType = "void"
    override def getOptions = null
    override def getId = this.id
    override def setId(id: Int) = this.id = id
    override def getTypeId = 0
  }
  override def getOverworldProvider: WorldProvider = new WorldProvider {
    var id: Int = _
    override def getType = "overworld"
    override def getOptions = null
    override def getId = this.id
    override def setId(id: Int) = this.id = id
    override def getTypeId = 0
  }
  override def getNetherProvider: WorldProvider = new WorldProvider {
    var id: Int = _
    override def getType = "nether"
    override def getOptions = null
    override def getId = this.id
    override def setId(id: Int) = this.id = id
    override def getTypeId = -1
  }
  override def getEndProvider: WorldProvider = new WorldProvider {
    var id: Int = _
    override def getType = "end"
    override def getOptions = null
    override def getId = this.id
    override def setId(id: Int) = this.id = id
    override def getTypeId = 1
  }
  override def getFlatProvider(pattern: String): WorldProvider = new WorldProvider {
    var id: Int = _
    override def getType = "flat"
    override def getOptions = pattern
    override def getId = this.id
    override def setId(id: Int) = this.id = id
    override def getTypeId = 0
  }
}
