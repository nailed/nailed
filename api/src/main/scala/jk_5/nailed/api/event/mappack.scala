package jk_5.nailed.api.event

import jk_5.eventbus.Event
import jk_5.nailed.api.mappack.Mappack

/**
 * No description given
 *
 * @author jk-5
 */
class MappackEvent(val mappack: Mappack) extends Event
case class MappackRegisteredEvent(private val _mappack: Mappack) extends MappackEvent(_mappack)
case class MappackUnregisteredEvent(private val _mappack: Mappack) extends MappackEvent(_mappack)
