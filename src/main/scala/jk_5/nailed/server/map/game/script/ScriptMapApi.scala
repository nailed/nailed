package jk_5.nailed.server.map.game.script

import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.server.map.NailedMap

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptMapApi(val map: NailedMap) {

  def sendChat(msg: String){
    map.broadcastChatMessage(new TextComponent(msg))
  }

  def sendChat(comp: BaseComponent){
    map.broadcastChatMessage(comp)
  }

  def sendChat(comp: BaseComponent*){
    map.broadcastChatMessage(comp: _*)
  }

  def sendChat(comp: Array[BaseComponent]){
    map.broadcastChatMessage(comp)
  }
}
