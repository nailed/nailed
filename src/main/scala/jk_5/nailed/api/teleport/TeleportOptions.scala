package jk_5.nailed.api.teleport

import jk_5.nailed.api.util.Location

/**
 * No description given
 *
 * @author jk-5
 */
case class TeleportOptions(private var destination: Location, private var clearInventory: Boolean = false) {

  def destination(destination: Location): TeleportOptions = {
    this.destination = destination
    this
  }

  def clearInventory(clearInventory: Boolean): TeleportOptions = {
    this.clearInventory = clearInventory
    this
  }

  def getDestination = this.destination
  def isClearInventory = this.clearInventory

  def copy = new TeleportOptions(destination, clearInventory)
}
