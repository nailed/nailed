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
