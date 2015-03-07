package jk_5.nailed.server.map.game.script.api

import java.util.concurrent.TimeUnit

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, TextComponent}
import jk_5.nailed.api.map.Team
import jk_5.nailed.api.util.{Location, TitleMessage}
import jk_5.nailed.server.map.game.script.ScriptingEngine
import jk_5.nailed.server.player.NailedPlayer
import org.mozilla.javascript.Function

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptTeamApi(private[this] val team: Team, private[api] val engine: ScriptingEngine) {

  def getId = team.id()
  def getName = team.name()

  def getPlayers: Array[ScriptPlayerApi] = {
    team.members.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer])).toArray
  }

  def forEachPlayer(function: Function){
    team.members.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer])).foreach(p => function.call(engine.context, engine.scope, engine.scope, Array(p)))
  }

  def broadcastChat(msg: String) = team.members().foreach(_.sendMessage(new TextComponent(msg)))
  def broadcastChat(comp: BaseComponent) = team.members().foreach(_.sendMessage(comp))
  def broadcastChat(comp: BaseComponent*) = team.members().foreach(_.sendMessage(comp: _*))
  def broadcastChat(comp: Array[BaseComponent]) = team.members().foreach(_.sendMessage(comp: _*))

  def broadcastSubtitle(msg: String) = team.members().foreach(_.displaySubtitle(new TextComponent(msg)))
  def broadcastSubtitle(comp: BaseComponent) = team.members().foreach(_.displaySubtitle(comp))
  def broadcastSubtitle(comp: BaseComponent*) = team.members().foreach(_.displaySubtitle(comp: _*))
  def broadcastSubtitle(comp: Array[BaseComponent]) = team.members().foreach(_.displaySubtitle(comp: _*))

  def setSubtitle(msg: String) = team.members().foreach(_.setSubtitle(new TextComponent(msg)))
  def setSubtitle(comp: BaseComponent) = team.members().foreach(_.setSubtitle(comp))
  def setSubtitle(comp: BaseComponent*) = team.members().foreach(_.setSubtitle(comp: _*))
  def setSubtitle(comp: Array[BaseComponent]) = team.members().foreach(_.setSubtitle(comp: _*))

  def clearSubtitle() = team.members().foreach(_.clearSubtitle())

  def countdown(seconds: Int, callback: Function){
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(1).setFadeOutTime(30)
    var ellapsed = 0

    def tick(){
      val comp = new TextComponent((seconds - ellapsed).toString)
      if(seconds - ellapsed == 5) comp.setColor(ChatColor.YELLOW)
      if(seconds - ellapsed == 4) comp.setColor(ChatColor.GOLD)
      if(seconds - ellapsed <= 3) comp.setColor(ChatColor.RED)
      val msg = builder.setTitle(comp).build()
      team.members.foreach(_.displayTitle(msg))
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
            team.members.foreach(_.displayTitle(msg))
            try{
              callback.call(engine.context, engine.scope, engine.scope, new Array[AnyRef](0))
            }catch{
              case e: Exception =>
                e.printStackTrace()
            }
          }
        }, 1, TimeUnit.SECONDS)
      }
    }

    tick()
  }

  def setSpawn(x: Double, y: Double, z: Double){
    team.setSpawnPoint(Location.builder().setX(x).setY(y).setZ(z).build())
  }

  def resetSpawn() = team.setSpawnPoint(null)
}
