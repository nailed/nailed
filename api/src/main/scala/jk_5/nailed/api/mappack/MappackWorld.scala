package jk_5.nailed.api.mappack

import jk_5.nailed.api.util.Location

/**
 * No description given
 *
 * @author jk-5
 */
trait MappackWorld {
  def name: String
  def generator: String
  def dimension: Int
  def spawnPoint: Location
}
