package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, TextComponent}
import jk_5.nailed.api.map.stat.ModifiableStat
import jk_5.nailed.api.util.TitleMessage
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.player.NailedPlayer

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptMapApi(private[this] val map: NailedMap) {

  private[this] val scoreboard = new ScriptScoreboardApi(this, map)

  def getScoreboard = scoreboard

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
    map.broadcastChatMessage(comp: _*)
  }

  def getPlayers: Array[ScriptPlayerApi] = {
    map.players.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer])).toArray
  }

  def getWorlds: Array[ScriptWorldApi] = {
    map.worlds.map(p => new ScriptWorldApi(p)).toArray
  }

  def getWorld(name: String): ScriptWorldApi = {
    val res = map.worlds.find(_.getConfig.name == name)
    if(res.isEmpty) return null
    new ScriptWorldApi(res.get)
  }

  def getTeam(name: String): ScriptTeamApi = {
    new ScriptTeamApi(map.getTeam(name))
  }

  def setUnreadyInterrupt(unreadyInterrupt: Boolean) = map.getGameManager.unreadyInterrupt = unreadyInterrupt
  def setWinInterrupt(winInterrupt: Boolean) = map.getGameManager.winInterrupt = winInterrupt

  def countdown(seconds: Int){
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(1).setFadeOutTime(30)
    var ellapsed = 0
    do{
      val comp = new TextComponent((seconds - ellapsed).toString)
      if(seconds - ellapsed == 5) comp.setColor(ChatColor.YELLOW)
      if(seconds - ellapsed == 4) comp.setColor(ChatColor.GOLD)
      if(seconds - ellapsed <= 3) comp.setColor(ChatColor.RED)
      val msg = builder.setTitle(comp).build()
      map.players.foreach(_.displayTitle(msg))
      ellapsed += 1
      Thread.sleep(1000)
    }while(ellapsed < seconds)
    val comp = new TextComponent("GO")
    comp.setColor(ChatColor.GREEN)
    val msg = builder.setTitle(comp).build()
    map.players.foreach(_.displayTitle(msg))
  }

  def broadcastSubtitle(msg: String) = map.players.foreach(_.displaySubtitle(new TextComponent(msg)))
  def broadcastSubtitle(comp: BaseComponent) = map.players.foreach(_.displaySubtitle(comp))
  def broadcastSubtitle(comp: BaseComponent*) = map.players.foreach(_.displaySubtitle(comp: _*))
  def broadcastSubtitle(comp: Array[BaseComponent]) = map.players.foreach(_.displaySubtitle(comp: _*))

  def setSubtitle(msg: String) = map.players.foreach(_.setSubtitle(new TextComponent(msg)))
  def setSubtitle(comp: BaseComponent) = map.players.foreach(_.setSubtitle(comp))
  def setSubtitle(comp: BaseComponent*) = map.players.foreach(_.setSubtitle(comp: _*))
  def setSubtitle(comp: Array[BaseComponent]) = map.players.foreach(_.setSubtitle(comp: _*))

  def clearSubtitle() = map.players.foreach(_.clearSubtitle())

  def enableStat(name: String){
    map.getStatManager.getStat(name) match {
      case s: ModifiableStat => s.enable()
      case _ =>
    }
  }
  def disableStat(name: String){
    map.getStatManager.getStat(name) match {
      case s: ModifiableStat => s.disable()
      case _ =>
    }
  }

  def setWinner(winner: Any){
    winner match {
      case p: ScriptPlayerApi => map.getGameManager.setWinner(NailedPlatform.getPlayerByName(p.getName))
      case t: ScriptTeamApi => map.getGameManager.setWinner(map.getTeam(t.getId))
      case _ => throw new RuntimeException(winner + " can not win games. Use Player or Team")
    }
  }
}
