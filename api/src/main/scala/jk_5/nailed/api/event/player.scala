package jk_5.nailed.api.event

import jk_5.eventbus.Event
import jk_5.eventbus.Event.Cancelable
import jk_5.nailed.api.chat._
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
class PlayerEvent(val player: Player) extends Event
case class PlayerJoinServerEvent(private val _player: Player) extends PlayerEvent(_player){
  var joinMessage: BaseComponent = new ComponentBuilder(this.player.getDisplayName).event(new HoverEvent(HoverEventAction.SHOW_TEXT, new TextComponent(this.player.getUniqueId.toString))).append(" joined the server").color(ChatColor.yellow).createFlat()
}
case class PlayerLeaveServerEvent(private val _player: Player) extends PlayerEvent(_player){
  var leaveMessage: BaseComponent = new ComponentBuilder(this.player.getDisplayName).event(new HoverEvent(HoverEventAction.SHOW_TEXT, new TextComponent(this.player.getUniqueId.toString))).append(" left the server").color(ChatColor.yellow).createFlat()
}
@Cancelable case class PlayerChatEvent(private val _player: Player, var message: String) extends PlayerEvent(_player)
