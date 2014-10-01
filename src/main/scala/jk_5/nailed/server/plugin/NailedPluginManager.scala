package jk_5.nailed.server.plugin

import java.io.File

import jk_5.nailed.api.event.plugin.RegisterAdditionalEventHandlersEvent
import jk_5.nailed.api.plugin.{Plugin, PluginContainer, PluginIdentifier, PluginManager}
import jk_5.nailed.server.NailedPlatform
import net.minecraft.launchwrapper.Launch
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedPluginManager extends PluginManager {

  private val plugins = mutable.HashSet[DefaultPluginContainer]()
  private val classLoader = Launch.classLoader
  private val logger = LogManager.getLogger

  override def getPlugin(identifier: scala.Any) = identifier match {
    case c: PluginContainer => c
    case i: PluginIdentifier => plugins.find(_.getIdentifier == i).orNull
    case n: String => plugins.find(_.getId == n).orNull
    case i =>  plugins.find(_.getInstance == i).orNull
  }

  override def getPlugins = java.util.Arrays.asList(plugins.toArray: _*)

  def loadPlugins(dir: File){
    PluginDiscoverer.clearDiscovered()
    PluginDiscoverer.discoverClasspathPlugins()
    PluginDiscoverer.discoverJarPlugins(dir)
    val discovered = PluginDiscoverer.getDiscovered
    for(p <- discovered){
      try{
        if(!p.isClasspath){
          classLoader.addURL(p.file.toURI.toURL)
        }
        val cl = classLoader.loadClass(p.className)
        val annotation = cl.getAnnotation(classOf[Plugin])
        val instance = cl.newInstance()
        val container = new DefaultPluginContainer(annotation, instance)
        injectPluginIdentifiers(container)
        logger.info(s"Successfully loaded plugin ${p.name} (id: ${p.id}, version: ${p.version})")
        plugins += container
      }catch{
        case e: InstantiationException =>
          logger.warn(s"Plugin ${p.id} (${p.name}, version: ${p.version}) could not be loaded")
          logger.warn(" Could not create an instance of the plugin. Is there a default constructor (no arguments)?")
          logger.trace(" Full exception:", e)
        case e: IllegalAccessException =>
          logger.warn(s"Plugin ${p.id} (${p.name}, version: ${p.version}) could not be loaded")
          logger.warn(" The default constructor was found, but it is not public")
          logger.trace(" Full exception:", e)
        case e: ClassNotFoundException =>
          logger.warn(s"Plugin ${p.id} (${p.name}, version: ${p.version}) could not be loaded")
          logger.warn(" Plugin class was discovered, but does not exist. Should not be possible in a normal environment")
          logger.trace(" Full exception:", e)
        case e: Exception =>
          logger.error(s"Plugin ${p.id} (${p.name}, version: ${p.version}) could not be loaded")
          logger.error(" An unknown error has occurred:", e)
      }
    }
    for(p <- plugins){
      p.fireEvent(new RegisterAdditionalEventHandlersEvent(p.getEventBus, NailedPlatform.globalEventBus))
    }
  }

  private def injectPluginIdentifiers(container: PluginContainer){
    val cl = container.getInstance().getClass
    try{
      for(f <- cl.getDeclaredFields){
        for(a <- f.getDeclaredAnnotations){
          if(a.annotationType() == classOf[PluginIdentifier.Instance]){
            f.setAccessible(true)
            f.set(container.getInstance(), container)
            logger.trace("Successfully injected PluginIdentifier into " + cl.getName + "." + f.getName)
          }
        }
      }
    }catch{
      case e: Exception => logger.warn("Unknown exception while injecting plugin identifiers into " + container.getId, e)
    }
  }

  def enablePlugins(){

  }
}
