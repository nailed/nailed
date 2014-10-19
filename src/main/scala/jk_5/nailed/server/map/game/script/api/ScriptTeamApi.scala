package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, TextComponent}
import jk_5.nailed.api.map.Team
import jk_5.nailed.api.util.{Location, TitleMessage}
import jk_5.nailed.server.player.NailedPlayer

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptTeamApi(private[this] val team: Team) {

  def getId = team.id()
  def getName = team.name()

  def getPlayers: Array[ScriptPlayerApi] = {
    team.members.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer])).toArray
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

  def countdown(seconds: Int){
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(1).setFadeOutTime(30)
    var ellapsed = 0
    do{
      val comp = new TextComponent((seconds - ellapsed).toString)
      if(seconds - ellapsed == 5) comp.setColor(ChatColor.YELLOW)
      if(seconds - ellapsed == 4) comp.setColor(ChatColor.GOLD)
      if(seconds - ellapsed <= 3) comp.setColor(ChatColor.RED)
      val msg = builder.setTitle(comp).build()
      team.members.foreach(_.displayTitle(msg))
      ellapsed += 1
      Thread.sleep(1000)
    }while(ellapsed < seconds)
    val comp = new TextComponent("GO")
    comp.setColor(ChatColor.GREEN)
    val msg = builder.setTitle(comp).build()
    team.members.foreach(_.displayTitle(msg))
  }

  def setSpawn(x: Double, y: Double, z: Double){
    team.setSpawnPoint(Location.builder().setX(x).setY(y).setZ(z).build())
  }

  def resetSpawn() = team.setSpawnPoint(null)
}
