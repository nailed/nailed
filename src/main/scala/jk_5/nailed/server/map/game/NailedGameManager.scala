package jk_5.nailed.server.map.game

import com.google.common.collect.ImmutableMap
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.map.stat.StatEvent
import jk_5.nailed.api.map.{GameManager, GameStartResult, GameWinnable, Team}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.TitleMessage
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.map.game.script.ScriptingEngine
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedGameManager(val map: NailedMap) extends GameManager {

  val logger = LogManager.getLogger
  override val getGameType = if(map.mappack != null) NailedPlatform.getGameTypeRegistry.getByName(map.mappack.getMetadata.gameType) else null
  override val hasCustomGameType = getGameType != null
  var isGameRunning = false
  val scriptingEngine = new ScriptingEngine(this)

  var winInterrupt = false
  var unreadyInterrupt = false
  var winner: GameWinnable = _

  override def startGame(): GameStartResult = {
    if(isGameRunning) return new GameStartResult(false, "A game is already running")
    try{
      val missingPlayers = mutable.ArrayBuffer[Player]()
      map.getTeams.foreach { team =>
        team.members().foreach { member =>
          if(member.getMap != map){
            missingPlayers += member
          }
        }
      }
      if(missingPlayers.length > 0){
        return new GameStartResult(false, "Game could not be started because the following players are not in the map: " + missingPlayers.map(_.getName).mkString(", "))
      }
      val scriptStarted = scriptingEngine.start()
      if(hasCustomGameType) getGameType.onGameStarted(map)
      isGameRunning = scriptStarted || hasCustomGameType
      if(!isGameRunning) return new GameStartResult(false, "game.js could not be found or read")
      map.getStatManager.fireEvent(new StatEvent("gameRunning", true))
      new GameStartResult(true, null)
    }catch{
      case e: Exception =>
        new GameStartResult(false, "Exception while starting game", e)
    }
  }

  override def endGame(): Boolean = {
    if(!isGameRunning) return false
    isGameRunning = false
    if(hasCustomGameType) getGameType.onGameEnded(map)
    scriptingEngine.kill()
    map.getStatManager.fireEvent(new StatEvent("gameRunning", false))
    cleanup()
    true
  }

  private[game] def onEnded(success: Boolean){
    if(!isGameRunning) return
    isGameRunning = false
    if(hasCustomGameType) getGameType.onGameEnded(map)
    map.getStatManager.fireEvent(new StatEvent("gameRunning", false))
    cleanup()
  }

  override def setWinner(winner: GameWinnable){
    if(this.winner != null) return
    this.winner = winner
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40)
    builder.setTitle(new ComponentBuilder("You Win!").color(ChatColor.GREEN).create(): _*)
    winner match {
      case p: Player =>
        p.displayTitle(builder.build())
        val b2 = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40)
        val t2 = b2.setTitle(new ComponentBuilder("You lost!").color(ChatColor.RED).create(): _*).build()
        map.players.filter(_ != p).foreach(_.displayTitle(t2))
      case t: Team =>
        val title = builder.build()
        t.members().foreach(_.displayTitle(title))
        val b2 = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40)
        val t2 = b2.setTitle(new ComponentBuilder("You lost!").color(ChatColor.RED).create(): _*).build()
        map.teams.values.filter(_ != t).map(_.members).flatten.foreach(_.displayTitle(t2))
        map.getStatManager.fireEvent(new StatEvent("teamWon", true, ImmutableMap.of[String, String]("team", t.id)))
    }
    map.broadcastChatMessage(new ComponentBuilder(winner.getName + " won the game").color(ChatColor.GOLD).create(): _*)
    onEnded(success = true)
    map.getStatManager.fireEvent(new StatEvent("gameHasWinner", true))
    if(winInterrupt){
      scriptingEngine.kill()
    }
  }

  override def setWinInterrupt(winInterrupt: Boolean) = this.winInterrupt = winInterrupt
  override def isWinInterrupt = winInterrupt

  override def setUnreadyInterrupt(unreadyInterrupt: Boolean) = this.unreadyInterrupt = unreadyInterrupt
  override def isUnreadyInterrupt = unreadyInterrupt

  def cleanup(){
    map.players.foreach(_.clearSubtitle())
  }
}
