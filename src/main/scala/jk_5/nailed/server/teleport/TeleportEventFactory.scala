package jk_5.nailed.server.teleport

import jk_5.nailed.api.event._
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.world.World
import jk_5.nailed.server.NailedEventFactory

/**
 * No description given
 *
 * @author jk-5
 */
object TeleportEventFactory {

  def isTeleportAllowed(origin: World, destination: World, entity: Player, options: TeleportOptions) = {
    !NailedEventFactory.fireEvent(new TeleportEventAllow(origin, destination, entity, options.copy)).isCanceled
  }

  def alterDestination(origin: World, destination: World, entity: Player, options: TeleportOptions) = {
    val newLoc = NailedEventFactory.fireEvent(new TeleportEventAlter(origin, destination, entity, options.copy)).location
    if(newLoc == null) options.getDestination else newLoc
  }

  def onLinkStart(origin: World, destination: World, entity: Player, options: TeleportOptions){
    NailedEventFactory.fireEvent(new TeleportEventStart(origin, destination, entity, options.copy))
  }

  def onExitWorld(origin: World, destination: World, entity: Player, options: TeleportOptions){
    NailedEventFactory.fireEvent(new TeleportEventExitWorld(origin, destination, entity, options.copy))
  }

  def onEnterWorld(origin: World, destination: World, entity: Player, options: TeleportOptions){
    NailedEventFactory.fireEvent(new TeleportEventEnterWorld(origin, destination, entity, options.copy))
  }

  def onEnd(origin: World, destination: World, entity: Player, options: TeleportOptions){
    NailedEventFactory.fireEvent(new TeleportEventEnd(origin, destination, entity, options.copy))
  }
}
