package jk_5.nailed.api.event

import jk_5.eventbus.Event
import jk_5.eventbus.Event.Cancelable
import jk_5.nailed.api.material.Material
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.Checks
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
@Cancelable case class BlockBreakEvent(x: Int, y: Int, z: Int, world: World, material: Material, meta: Byte, player: Player) extends Event {
  Checks.check(material.isBlock, "Given material is not a block")
}
//TODO: add ItemStack to place event?
@Cancelable case class BlockPlaceEvent(x: Int, y: Int, z: Int, world: World, player: Player) extends Event
