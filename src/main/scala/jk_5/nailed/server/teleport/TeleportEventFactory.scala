/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.teleport

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.TeleportOptions
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
