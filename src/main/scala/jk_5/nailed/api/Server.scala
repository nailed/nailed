package jk_5.nailed.api

import java.io.File
import java.util.UUID

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.PluginManager
import jk_5.nailed.api.world.DimensionManager
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

abstract class Server {

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
   * @return a player object if one was found, null otherwise
   */
  def getPlayer(id: UUID): Player

  /**
   * Gets the dimensionmanager that is responsible for registering and controlling custom dimensions
   *
   * @return the DimensionManager instance
   */
  def getDimensionManager: DimensionManager
}
