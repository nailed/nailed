package jk_5.nailed.api.mappack

/**
 * No description given
 *
 * @author jk-5
 */
trait MappackMetadata {
  def name: String
  def version: String
  def authors: Array[MappackAuthor]
  def worlds: Array[MappackWorld]
  def teams: Array[MappackTeam]
}
