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

package jk_5.nailed.api

import java.io.File
import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.map.MapLoader
import jk_5.nailed.api.mappack.MappackRegistry
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.PluginManager
import jk_5.nailed.api.scheduler.Scheduler
import jk_5.nailed.api.util.{Checks, PlayerSelector}
import jk_5.nailed.api.world.{DefaultWorldProviders, World, WorldContext, WorldProvider}

/**
 * No description given
 *
 * @author jk-5
 */
object Server {
  private var instance: Server = _

  /**
   * Sets the server instance. This method may only be called once per an
   * application.
   *
   * @param instance the new instance to set
   */
  def setInstance(instance: Server){
    Checks.notNull(instance, "instance")
    Checks.check(this.instance == null, "Instance is already set")
    this.instance = instance
  }

  def getInstance = this.instance
}

trait Server {

  /**
   * Gets the name of the currently running server software.
   *
   * @return the name of this instance
   */
  def getName: String

  /**
   * Gets the version of the currently running proxy software.
   *
   * @return the version of this instance
   */
  def getVersion: String

  /**
   * Get the {@link PluginManager} associated with loading plugins and
   * dispatching events. It is recommended that implementations use the
   * provided PluginManager class.
   *
   * @return the plugin manager
   */
  def getPluginManager: PluginManager

  /**
   * Return the folder used to load plugins from.
   *
   * @return the folder used to load plugin
   */
  def getPluginsFolder: File

  /**
   * Gets the player with the given UUID.
   *
   * @param id UUID of the player to retrieve
   * @return Some(player) if a player was found, None otherwise
   */
  def getPlayer(id: UUID): Option[Player]

  /**
   * Gets the player with the given username.
   *
   * @param name Username of the player to retrieve
   * @return Some(player) if a player was found, None otherwise
   */
  def getPlayerByName(name: String): Option[Player]

  /**
   * Gets all currently online players
   *
   * @return an array containing all online players
   */
  def getOnlinePlayers: Array[Player]

  /**
   * Broadcasts a chat message across the entire server
   *
   * @param message the message to broadcast
   */
  def broadcastMessage(message: BaseComponent)
  def broadcastMessage(message: BaseComponent*)
  def broadcastMessage(message: Array[BaseComponent])

  def getScheduler: Scheduler

  def getWorld(dimensionId: Int): World

  def getDefaultWorldProviders: DefaultWorldProviders

  def createNewWorld(provider: WorldProvider, ctx: WorldContext): World

  def getMapLoader: MapLoader

  def getMappackRegistry: MappackRegistry

  def getConsoleCommandSender: CommandSender

  def getPlayerSelector: PlayerSelector

  def isAsync: Boolean
}
