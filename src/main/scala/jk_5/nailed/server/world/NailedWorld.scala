package jk_5.nailed.server.world

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.DefaultGameRules
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.{World, WorldContext, WorldProvider}
import jk_5.nailed.server.map.gamerule.WrappedEditableGameRules
import net.minecraft.world.WorldServer

/**
 * No description given
 *
 * @author jk-5
 */
class NailedWorld(var wrapped: WorldServer, val context: WorldContext = null) extends World {

  private var map: Option[Map] = None

  private val provider: Option[WorldProvider] = wrapped.provider match {
    case p: DelegatingWorldProvider => Some(p.wrapped)
    case _ => None
  }

  override val getGameRules = if(this.context != null && this.context.config != null) new WrappedEditableGameRules(context.config.gameRules) else new WrappedEditableGameRules(DefaultGameRules)

  override def getDimensionId = wrapped.provider.dimensionId
  override def getName = "world_" + getDimensionId
  override def getPlayers = List[Player]()
  override def getType = this.provider match {
    case Some(p) => p.getTypeId
    case None => 0
  }

  override def setMap(map: Map) = this.map = Some(map)
  override def getMap = this.map
  override def getConfig: MappackWorld = if(this.context == null) null else this.context.config

  override def toString = s"NailedWorld{id=$getDimensionId,name=$getName,type=$getType,gameRules=$getGameRules}"
}
