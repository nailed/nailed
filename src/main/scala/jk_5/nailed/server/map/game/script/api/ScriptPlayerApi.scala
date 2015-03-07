package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.GameMode
import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.server.player.NailedPlayer

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptPlayerApi(private[this] val player: NailedPlayer) {

  def getName = player.getName

  def sendChat(msg: String) = player.sendMessage(new TextComponent(msg))
  def sendChat(comp: BaseComponent) = player.sendMessage(comp)
  def sendChat(comp: BaseComponent*) = player.sendMessage(comp: _*)
  def sendChat(comp: Array[BaseComponent]) = player.sendMessage(comp: _*)

  def displaySubtitle(msg: String) = player.displaySubtitle(new TextComponent(msg))
  def displaySubtitle(comp: BaseComponent) = player.displaySubtitle(comp)
  def displaySubtitle(comp: BaseComponent*) = player.displaySubtitle(comp: _*)
  def displaySubtitle(comp: Array[BaseComponent]) = player.displaySubtitle(comp: _*)

  def setSubtitle(msg: String) = player.setSubtitle(new TextComponent(msg))
  def setSubtitle(comp: BaseComponent) = player.setSubtitle(comp)
  def setSubtitle(comp: BaseComponent*) = player.setSubtitle(comp: _*)
  def setSubtitle(comp: Array[BaseComponent]) = player.setSubtitle(comp: _*)

  def clearSubtitle() = player.clearSubtitle()

  def clearInventory() = player.clearInventory()
  def setGamemode(gamemode: GameMode) = player.setGameMode(gamemode)
  def setHealth(health: Double) = player.setHealth(health)
  def setHunger(hunger: Double) = player.setHunger(hunger)
  def setLevel(level: Int) = player.setLevel(level)
  def setAllowedToFly(allowedToFly: Boolean) = player.setAllowedToFly(allowedToFly)
}
