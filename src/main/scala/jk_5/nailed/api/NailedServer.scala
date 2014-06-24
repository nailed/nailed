package jk_5.nailed.api

import java.io.File

import org.apache.commons.lang3.Validate

/**
 * No description given
 *
 * @author jk-5
 */
object NailedServer {
  private var instance: NailedServer = _

  /**
   * Sets the server instance. This method may only be called once per an
   * application.
   *
   * @param instance the new instance to set
   */
  def setInstance(instance: NailedServer){
    Validate.notNull(instance, "instance")
    Validate.validState(this.instance == null, "Instance is already set")
    this.instance = instance
  }

  def getInstance = this.instance
}

abstract class NailedServer {

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


}
