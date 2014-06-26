package jk_5.nailed.server

import java.io.File
import java.util.UUID

import jk_5.nailed.api.Server
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.PluginManager
import jk_5.nailed.server.tweaker.{NailedTweaker, NailedVersion}
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.server.dedicated.DedicatedServer

/**
 * No description given
 *
 * @author jk-5
 */
object NailedServer extends Server {

  private val pluginsFolder = new File(NailedTweaker.gameDir, "plugins")
  private val pluginManager = new PluginManager(this)

  NailedEventFactory.server = this
  Server.setInstance(this)

  /**
   * Gets the name of the currently running server software.
   *
   * @return the name of this instance
   */
  override def getName = "Nailed"

  /**
   * Return the folder used to load plugins from.
   *
   * @return the folder used to load plugin
   */
  override def getPluginsFolder = this.pluginsFolder

  /**
   * Gets the version of the currently running proxy software.
   *
   * @return the version of this instance
   */
  override def getVersion = NailedVersion.full

  /**
   * Get the {@link PluginManager} associated with loading plugins and
   * dispatching events. It is recommended that implementations use the
   * provided PluginManager class.
   *
   * @return the plugin manager
   */
  override def getPluginManager = this.pluginManager

  /**
   * Gets the player with the given UUID.
   *
   * @param id UUID of the player to retrieve
   * @return a player object if one was found, null otherwise
   */
  override def getPlayer(id: UUID): Player = null //TODO

  /**
   * Gets the dimensionmanager that is responsible for registering and controlling custom dimensions
   *
   * @return the DimensionManager instance
   */
  override def getDimensionManager = NailedDimensionManager

  def register(){

  }

  def preLoad(server: DedicatedServer){
    this.pluginsFolder.mkdir()
    this.pluginManager.discoverClasspathPlugins()
    this.pluginManager.discoverPlugins(this.pluginsFolder)
    this.pluginManager.loadPlugins()
  }

  def load(server: DedicatedServer){
    this.pluginManager.enablePlugins()

    this.getDimensionManager.registerDimension(2, 0)
    this.getDimensionManager.initWorld(2)
  }
}
