package jk_5.nailed.server.plugin

import java.io.{File, FileOutputStream, InputStream, InputStreamReader}
import java.nio.channels.Channels
import java.util.jar.JarFile

import com.typesafe.config.{Config, ConfigFactory}
import jk_5.nailed.api.event.plugin.RegisterAdditionalEventHandlersEvent
import jk_5.nailed.api.plugin._
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
  private var pluginDir: File = _

  override def getPlugin(identifier: scala.Any) = identifier match {
    case c: PluginContainer => c
    case i: PluginIdentifier => plugins.find(_.getIdentifier == i).orNull
    case n: String => plugins.find(_.getId == n).orNull
    case i =>  plugins.find(_.getInstance == i).orNull
  }

  override def getPlugins = java.util.Arrays.asList(plugins.toArray: _*)

  def loadPlugins(dir: File){
    this.pluginDir = dir
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
        val container = new DefaultPluginContainer(annotation, instance, if(!p.isClasspath) p.file else null)
        var success = injectPluginIdentifiers(container) && injectConfiguration(container)
        if(success){
          logger.info(s"Successfully loaded plugin ${p.name} (id: ${p.id}, version: ${p.version})")
          plugins += container
        }else{
          logger.warn(s"Skipping plugin ${p.name}")
        }
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

  private def injectPluginIdentifiers(container: PluginContainer): Boolean = {
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
      true
    }catch{
      case e: Exception =>
        logger.warn("Unknown exception while injecting plugin identifiers into " + container.getId, e)
        false
    }
  }

  private def injectConfiguration(container: DefaultPluginContainer): Boolean = {
    val cl = container.getInstance().getClass
    try{
      for(f <- cl.getDeclaredFields){
        for(a <- f.getDeclaredAnnotations){
          if(a.annotationType() == classOf[Configuration]){
            val ann = a.asInstanceOf[Configuration]
            val pluginDir = new File(this.pluginDir, container.getId)
            val config = try{
              initPluginConfig(container, new File(pluginDir, ann.filename()), ann.defaults())
            }catch{
              case e: RuntimeException =>
                logger.error("")
                logger.error("Error while injecting plugin config for " + container.getName + ":")
                logger.error(e.getMessage)
                logger.error("This plugin will not be loaded!")
                logger.error("")
                null
            }
            if(config == null) return false
            f.setAccessible(true)
            f.set(container.getInstance(), config)
            logger.trace("Successfully injected plugin config into " + cl.getName + "." + f.getName)
          }
        }
      }
      true
    }catch{
      case e: Exception =>
        logger.warn("Unknown exception while injecting plugin configuration into " + container.getId, e)
        false
    }
  }

  def enablePlugins(){

  }

  private def initPluginConfig(container: DefaultPluginContainer, configLocation: File, defaultPath: String): Config = {
    var defaultInput: InputStream = null
    def getDefaultInput = if(container.hasLocation) getConfigFromJar(container.getLocation, defaultPath) else NailedPluginManager.getClass.getResourceAsStream("/" + defaultPath)
    try{
      defaultInput = getDefaultInput
      if(defaultInput == null) throw new RuntimeException("Default configuration path " + defaultPath + " could not be found")
      if(!configLocation.exists() || configLocation.length() == 0){
        configLocation.getParentFile.mkdirs()
        val inChannel = Channels.newChannel(getDefaultInput)
        val out = new FileOutputStream(configLocation).getChannel
        out.transferFrom(inChannel, 0, Long.MaxValue)
        out.close()
      }
      val defaults = ConfigFactory.parseReader(new InputStreamReader(defaultInput))
      val conf = ConfigFactory.parseFile(configLocation).withFallback(defaults)
      conf
    }finally{
      if(defaultInput != null) defaultInput.close()
    }
  }

  private def getConfigFromJar(jar: File, entry: String): InputStream = {
    try{
      val jarFile = new JarFile(jar)
      jarFile.getInputStream(jarFile.getEntry(entry))
    }catch{
      case e: Exception => null
    }
  }
}
