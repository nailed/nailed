package jk_5.nailed.tweaker

import java.io.File
import java.util

import jk_5.nailed.api.plugin.PluginClassLoader
import net.minecraft.launchwrapper.{ITweaker, Launch, LaunchClassLoader}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class NailedTweaker extends ITweaker {

  val logger = LogManager.getLogger
  var gameDir: File = _
  lazy val classLoader = Launch.classLoader

  override def acceptOptions(args: util.List[String], gameDir: File, assetsDir: File, profile: String){
    this.gameDir = gameDir
    NailedVersion.readConfig()

    logger.info(s"Initializing Nailed version ${NailedVersion.full}")
    if(NailedVersion.isSnapshot){
      logger.info("This is a snapshot version. It might be instable/buggy")
    }
  }

  override def injectIntoClassLoader(classLoader: LaunchClassLoader){
    PluginClassLoader.register()
  }

  override def getLaunchArguments = new Array[String](0)
  override def getLaunchTarget = "net.minecraft.server.MinecraftServer"
}
