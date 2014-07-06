package jk_5.nailed.api.event

import jk_5.eventbus.Event
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class WorldEvent(world: World) extends Event
case class WorldPreTickEvent(private val _world: World) extends WorldEvent(_world)
case class WorldPostTickEvent(private val _world: World) extends WorldEvent(_world)
