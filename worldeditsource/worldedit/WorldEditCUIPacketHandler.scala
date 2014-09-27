package jk_5.nailed.worldedit

import io.netty.util.CharsetUtil
import jk_5.nailed.api.messaging.PluginMessageListener
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
object WorldEditCUIPacketHandler extends PluginMessageListener {

  override def onPluginMessageReceived(channel: String, player: Player, message: Array[Byte]){
    val session = WorldEditPlayer.getSession(player)
    if(session.hasCUISupport) return
    session.handleCUIInitializationMessage(new String(message, CharsetUtil.UTF_8))
  }
}
