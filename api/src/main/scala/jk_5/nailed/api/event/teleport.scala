package jk_5.nailed.api.event

import jk_5.eventbus.Event
import jk_5.eventbus.Event.Cancelable
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class TeleportEvent(val oldWorld: World, val newWorld: World, val entity: Player, val options: TeleportOptions) extends Event
@Cancelable case class TeleportEventAllow(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
case class TeleportEventAlter(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions, var location: Location = null) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
case class TeleportEventStart(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
case class TeleportEventExitWorld(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
case class TeleportEventEnterWorld(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
case class TeleportEventEnd(private val _oldWorld: World, private val _newWorld: World, private val _entity: Player, private val _options: TeleportOptions) extends TeleportEvent(_oldWorld, _newWorld, _entity, _options)
