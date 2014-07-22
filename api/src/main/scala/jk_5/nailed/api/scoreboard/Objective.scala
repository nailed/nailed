package jk_5.nailed.api.scoreboard

/**
 * This represents an objective in the scoreboard system.
 * An objective is a score type that can be displayed on the client gui
 * The individual scores can be set for either players or other names (this depends on the user of the api)
 *
 * @author jk-5
 */
trait Objective {

  /**
   * Get the unique id for this Objective.
   *
   * @return The unique id for this objective. This id may be chosen upon creation in `ScoreboardManager#getOrCreateObjective(String)}`
   */
  def id: String

  /**
   * Get the display name for this objective. Note that this may be formatted (color, bold, italic).
   *
   * @return The displayName of this Objective.
   */
  def displayName: String

  /**
   * Sets the displayName of this Objective.
   * Note that the displayName may not be bigger than 32.
   *
   * @param displayName The new displayName for this objective.
   * @throws IllegalArgumentException When the displayName is longer than 32.
   */
  def setDisplayName(displayName: String)

  /**
   * Gets the score object for the specified name, or creating one if it doesn't already exist.
   * Note that the name may not be bigger than 16.
   *
   * @param name The name of the score object. (<= 16).
   * @return The score object.
   * @throws IllegalArgumentException When the name is longer than 16.
   */
  def score(name: String): Score

  /**
   * Removes the score from the objective.
   *
   * @param score The score to remove.
   */
  def removeScore(score: Score)
}