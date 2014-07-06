package jk_5.nailed.api

import java.io.File
import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.MapLoader
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.PluginManager
import jk_5.nailed.api.scheduler.Scheduler
import jk_5.nailed.api.world.{DefaultWorldProviders, World, WorldContext, WorldProvider}
import org.apache.commons.lang3.Validate

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
    Validate.notNull(instance, "instance")
    Validate.validState(this.instance == null, "Instance is already set")
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

  def getScheduler: Scheduler

  def getWorld(dimensionId: Int): World

  def getDefaultWorldProviders: DefaultWorldProviders

  def createNewWorld(provider: WorldProvider, ctx: WorldContext): World

  def getMapLoader: MapLoader
}
