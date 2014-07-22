package jk_5.nailed.api.scoreboard

/**
 * No description given
 *
 * @author jk-5
 */
trait ScoreboardManager {

  def getOrCreateObjective(id: String): Objective
  def getObjective(id: String): Option[Objective]

  /**
   * Displays the given objective at the given location
   * Pass null as the objective to clear the display at that slot
   *
   * @param display   The slot to display the objective
   * @param objective The objective to display
   */
  def setDisplay(display: DisplayType, objective: Objective)

  def getOrCreateTeam(id: String): ScoreboardTeam
  def getTeam(id: String): Option[ScoreboardTeam]
}
