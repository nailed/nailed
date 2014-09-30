package jk_5.nailed.server.map.game

import jk_5.nailed.api.map.GameManager
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.map.game.script.ScriptingEngine
import org.apache.logging.log4j.LogManager

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
    isGameRunning = false
    if(hasCustomGameType) getGameType.onGameEnded(map)
  }
}
