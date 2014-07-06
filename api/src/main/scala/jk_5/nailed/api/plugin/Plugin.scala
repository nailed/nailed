package jk_5.nailed.api.plugin

import java.io.{File, InputStream}

import jk_5.nailed.api.Server
import org.apache.logging.log4j.{LogManager, Logger}

/**
 * Represents any Plugin that may be loaded at runtime to enhance existing
 * functionality.
 *
 * @author jk-5
 */
abstract class Plugin {

  private var description: PluginDescription = _
  private var server: Server = _
  private var file: File = _
  private var logger: Logger = _

  /**
   * Called when the plugin has just been loaded. Most of the proxy will not
   * be initialized, so only use it for registering configuration and other
   * predefined behavior.
   */
  def onLoad(){}

  /**
   * Called when this plugin is enabled.
   */
  def onEnable(){}

  /**
   * Called when this plugin is disabled.
   */
  def onDisable(){}

  /**
   * Gets the data folder where this plugin may store arbitrary data. It will
   * be a child of {@link NailedServer#getPluginsFolder()}.
   *
   * @return the data folder of this plugin
   */
  final def getDataFolder = new File(getServer.getPluginsFolder, getDescription.getName)

  /**
   * Get a resource from within this plugins jar or container. Care must be
   * taken to close the returned stream.
   *
   * @param name the full path name of this resource
   * @return the stream for getting this resource, or null if it does not
   * exist
   */
  final def getResourceAsStream(name: String): InputStream = {
    getClass.getClassLoader.getResourceAsStream(name)
  }

  private[plugin] final def init(server: Server, description: PluginDescription){
    this.server = server
    this.description = description
    this.file = description.getFile
    this.logger = LogManager.getLogger("Plugin: " + getDescription.getName)
  }

  def getDescription = this.description
  def getServer = this.server
  def getFile = this.file
  def getLogger = this.logger
  def getPluginManager = this.server.getPluginManager
}
