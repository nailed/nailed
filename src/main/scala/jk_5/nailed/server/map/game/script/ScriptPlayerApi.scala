package jk_5.nailed.server.map.game.script

import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.server.player.NailedPlayer

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptPlayerApi(private val player: NailedPlayer) {

  def sendChat(msg: String){
    player.sendMessage(new TextComponent(msg))
  }

  def sendChat(comp: BaseComponent){
    player.sendMessage(comp)
  }

  def sendChat(comp: BaseComponent*){
    player.sendMessage(comp: _*)
  }

  def sendChat(comp: Array[BaseComponent]){
    player.sendMessage(comp)
  }
}
