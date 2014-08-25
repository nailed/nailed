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
