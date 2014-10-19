package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.scoreboard.DisplayType
import jk_5.nailed.server.map.NailedMap

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptScoreboardApi(private[this] val mapApi: ScriptMapApi, private[this] val map: NailedMap) {

  def getObjective(name: String) = {
    val o = map.getScoreboardManager.getOrCreateObjective(name)
    if(o == null) null else new ScriptObjectiveApi(o)
  }

  def setDisplay(objective: ScriptObjectiveApi, display: DisplayType){
    map.getScoreboardManager.setDisplay(display, map.getScoreboardManager.getObjective(objective.getObjectiveId))
  }
}
