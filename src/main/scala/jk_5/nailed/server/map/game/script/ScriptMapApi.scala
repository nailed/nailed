package jk_5.nailed.server.map.game.script

import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptMapApi(private val map: NailedMap) {

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

  def getPlayers: Array[ScriptPlayerApi] = {
    map.players.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer]))
  }

  def getWorlds: Array[ScriptWorldApi] = {
    map.worlds.map(p => new ScriptWorldApi(p.asInstanceOf[NailedWorld]))
  }

  def getWorld(name: String): ScriptWorldApi = {
    new ScriptWorldApi(map.worlds.find(_.getName == name).orNull.asInstanceOf[NailedWorld])
  }
}
