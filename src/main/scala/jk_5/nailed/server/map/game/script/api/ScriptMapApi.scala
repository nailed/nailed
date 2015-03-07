package jk_5.nailed.server.map.game.script.api

import java.util.concurrent.TimeUnit

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, TextComponent}
import jk_5.nailed.api.map.stat.ModifiableStat
import jk_5.nailed.api.util.TitleMessage
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.map.game.script.ScriptingEngine
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld
import org.mozilla.javascript.Function

import scala.collection.convert.wrapAsScala._

class ScriptMapApi(private[this] val map: NailedMap, private[api] val engine: ScriptingEngine) extends EventEmitter {

  map.eventEmitter = this

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
    map.worlds.map(p => new ScriptWorldApi(p.asInstanceOf[NailedWorld])).toArray
  }

  def getWorld(name: String): ScriptWorldApi = {
    val res = map.worlds.find(_.getConfig.name == name)
    if(res.isEmpty) return null
    new ScriptWorldApi(res.get.asInstanceOf[NailedWorld])
  }

  def getTeam(name: String): ScriptTeamApi = {
    new ScriptTeamApi(map.getTeam(name), engine)
  }

  def setUnreadyInterrupt(unreadyInterrupt: Boolean) = map.getGameManager.unreadyInterrupt = unreadyInterrupt
  def setWinInterrupt(winInterrupt: Boolean) = map.getGameManager.winInterrupt = winInterrupt

  def countdown(seconds: Int, callback: Function){
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(1).setFadeOutTime(30)
    var ellapsed = 0

    def tick(){
      val comp = new TextComponent((seconds - ellapsed).toString)
      if(seconds - ellapsed == 5) comp.setColor(ChatColor.YELLOW)
      if(seconds - ellapsed == 4) comp.setColor(ChatColor.GOLD)
      if(seconds - ellapsed <= 3) comp.setColor(ChatColor.RED)
      val msg = builder.setTitle(comp).build()
      map.players.foreach(_.displayTitle(msg))
      ellapsed += 1
      if(ellapsed < seconds){
        engine.executor.schedule(new Runnable {
          def run() = tick()
        }, 1, TimeUnit.SECONDS)
      }else{
        engine.executor.schedule(new Runnable {
          def run(){
            val comp = new TextComponent("GO")
            comp.setColor(ChatColor.GREEN)
            val msg = builder.setTitle(comp).build()
            map.players.foreach(_.displayTitle(msg))
            callback.call(engine.context, engine.scope, engine.scope, new Array[AnyRef](0))
          }
        }, 1, TimeUnit.SECONDS)
      }
    }

    tick()
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
