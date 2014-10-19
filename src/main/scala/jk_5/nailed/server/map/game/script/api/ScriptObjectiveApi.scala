package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.scoreboard.Objective

/**
 * No description given
 *
 * @author jk-5
 */
class ScriptObjectiveApi(private[this] val objective: Objective) {

  def setDisplayName(name: String) = objective.setDisplayName(name)
  def set(score: String, value: Int) = objective.getScore(score).setValue(value)
  def add(score: String, value: Int) = objective.getScore(score).addValue(value)
  def get(score: String) = objective.getScore(score).getValue

  def getObjectiveId = objective.getId
}
