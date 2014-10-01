package jk_5.nailed.server.plugin

import java.io.File

import jk_5.nailed.api.plugin.{PluginContainer, PluginManager}
import org.slf4j.LoggerFactory

/**
 * No description given
 *
 * @author jk-5
 */
object NailedPluginManager extends PluginManager {

  override def getPlugin(identifier: scala.Any) = ???

  override def getPlugins = ???

  override def getLogger(plugin: PluginContainer) = LoggerFactory.getLogger(plugin.getId)

  def discoverClasspathPlugins(){

  }

  def discoverPlugins(dir: File){

  }

  def loadPlugins(){

  }

  def enablePlugins(){

  }
}
