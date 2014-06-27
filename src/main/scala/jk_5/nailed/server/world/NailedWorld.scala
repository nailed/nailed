package jk_5.nailed.server.world

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World
import net.minecraft.world.WorldServer

/**
 * No description given
 *
 * @author jk-5
 */
class NailedWorld(var wrapped: WorldServer) extends World {

  override def getDimensionId = wrapped.provider.dimensionId
  override def getName = "world_" + getDimensionId
  override def getPlayers = List[Player]()
}
