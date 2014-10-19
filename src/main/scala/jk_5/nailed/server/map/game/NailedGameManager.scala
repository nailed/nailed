package jk_5.nailed.server.map.game

import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.map.{GameManager, GameWinnable, Team}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.TitleMessage
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.map.game.script.ScriptingEngine
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._

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

  override def startGame(): Boolean = {
    if(isGameRunning) return false
    try{
      val scriptStarted = scriptingEngine.start()
      if(hasCustomGameType) getGameType.onGameStarted(map)
      isGameRunning = scriptStarted || hasCustomGameType
      isGameRunning
    }catch{
      case e: Exception =>
        logger.error("Exception while starting game for " + map.toString, e)
        false
    }
  }

  override def endGame(): Boolean = {
    if(!isGameRunning) return false
    isGameRunning = false
    if(hasCustomGameType) getGameType.onGameEnded(map)
    scriptingEngine.kill()
    true
  }

  private[game] def onEnded(success: Boolean){
    if(!isGameRunning) return
    isGameRunning = false
    if(hasCustomGameType) getGameType.onGameEnded(map)
  }

  override def setWinner(winner: GameWinnable){
    if(this.winner != null) return
    this.winner = winner
    val builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40)
    builder.setTitle(new ComponentBuilder("You won!").color(ChatColor.GREEN).create(): _*)
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
    }
    map.broadcastChatMessage(new ComponentBuilder(winner.getName + " won the game").color(ChatColor.GOLD).create(): _*)
    onEnded(success = true)
    if(winInterrupt){
      scriptingEngine.kill()
    }
  }

  override def setWinInterrupt(winInterrupt: Boolean) = this.winInterrupt = winInterrupt
  override def isWinInterrupt = winInterrupt

  override def setUnreadyInterrupt(unreadyInterrupt: Boolean) = this.unreadyInterrupt = unreadyInterrupt
  override def isUnreadyInterrupt = unreadyInterrupt
}
