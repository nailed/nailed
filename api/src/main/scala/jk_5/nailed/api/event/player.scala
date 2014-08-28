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
import jk_5.nailed.api.chat._
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.material.ItemStack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class PlayerEvent(val player: Player) extends Event
case class PlayerJoinServerEvent(private val _player: Player) extends PlayerEvent(_player){
  var joinMessage: BaseComponent = {
    val base = new TextComponent("")
    val name = new TextComponent(this.player.getDisplayName)
    name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.player.getUniqueId.toString)))
    val joined = new TextComponent(" joined the server")
    joined.setColor(ChatColor.YELLOW)
    base.addExtra(name)
    base.addExtra(joined)
    base
  }
}
case class PlayerLeaveServerEvent(private val _player: Player) extends PlayerEvent(_player){
  var leaveMessage: BaseComponent = {
    val base = new TextComponent("")
    val name = new TextComponent(this.player.getDisplayName)
    name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.player.getUniqueId.toString)))
    val left = new TextComponent(" left the server")
    left.setColor(ChatColor.YELLOW)
    base.addExtra(name)
    base.addExtra(left)
    base
  }
}
@Cancelable case class PlayerChatEvent(private val _player: Player, var message: String) extends PlayerEvent(_player)
case class PlayerJoinMapEvent(private val _player: Player, map: Map) extends PlayerEvent(_player)
case class PlayerLeaveMapEvent(private val _player: Player, map: Map) extends PlayerEvent(_player)
case class PlayerJoinWorldEvent(private val _player: Player, world: World) extends PlayerEvent(_player)
case class PlayerLeaveWorldEvent(private val _player: Player, world: World) extends PlayerEvent(_player)
@Cancelable case class PlayerThrowItemEvent(private val _player: Player, stack: ItemStack) extends PlayerEvent(_player)
@Cancelable case class PlayerRightClickItemEvent(private val _player: Player, stack: ItemStack) extends PlayerEvent(_player)
