package jk_5.nailed.api.mappack

import jk_5.nailed.api.mappack.tutorial.Tutorial

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
  def tutorial: Tutorial
}
