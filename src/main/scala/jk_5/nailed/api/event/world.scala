package jk_5.nailed.api.event

import jk_5.eventbus.Event
import net.minecraft.world.WorldServer

/**
 * No description given
 *
 * @author jk-5
 */
//TODO: replace this world with our own api world. Don't let api depend on mc
class WorldEvent(world: WorldServer) extends Event
case class WorldPreTickEvent(private val _world: WorldServer) extends WorldEvent(_world)
case class WorldPostTickEvent(private val _world: WorldServer) extends WorldEvent(_world)
