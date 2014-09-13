package jk_5.nailed.api.map.stat

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
abstract sealed class Stat(val name: String) {

  protected val listeners = mutable.ArrayBuffer[StatListener]()
  protected var state = false

  def registerListener(listener: StatListener) = listeners += listener
  def unregisterListener(listener: StatListener) = listeners -= listener

  def isEnabled = state
  def isDisabled = !state
}

final class SubscribedStat(_name: String, val track: String, val attributes: java.util.Map[String, String]) extends Stat(_name) {

  def onEvent(event: StatEvent){
    if(event.name != track) return
    if(this.state == event.state) return
    state = event.state
    if(state) listeners.foreach(_.onEnable()) else listeners.foreach(_.onDisable())
  }
}

final class ModifiableStat(_name: String) extends Stat(_name) {

  def enable(){
    if(isEnabled) return
    state = true

    listeners.foreach(_.onEnable())
  }

  def disable(){
    if(isDisabled) return
    state = false

    listeners.foreach(_.onDisable())
  }
}
