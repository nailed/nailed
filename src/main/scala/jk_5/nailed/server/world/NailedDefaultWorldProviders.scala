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
