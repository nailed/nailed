package jk_5.nailed.api.mappack.tutorial

import jk_5.nailed.api.util.Location

/**
 * No description given
 *
 * @author jk-5
 */
trait TutorialStage {
  def title: String
  def messages: Array[String]
  def teleport: Location
}
