package jk_5.nailed.server.tweaker

import java.io.{File, IOException}
import java.util

import jk_5.nailed.server.tweaker.patcher.BinPatchManager
import jk_5.nailed.server.tweaker.remapping.NameRemapper
import jk_5.nailed.server.tweaker.transformer.AccessTransformer
import net.minecraft.launchwrapper.{ITweaker, Launch, LaunchClassLoader}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object NailedTweaker {
  var gameDir: File = _
  var deobf = false
  lazy val classLoader = Launch.classLoader
}

class NailedTweaker extends ITweaker {

  val logger = LogManager.getLogger

  override def acceptOptions(args: util.List[String], gameDir: File, assetsDir: File, profile: String){
    NailedTweaker.gameDir = gameDir
    NailedVersion.readConfig()

    logger.info(s"Initializing Nailed version ${NailedVersion.full}")
    if(NailedVersion.isSnapshot){
      logger.info("This is a snapshot version. It might be instable/buggy")
    }

    BinPatchManager.setup()
    NameRemapper.init()
  }

  override def injectIntoClassLoader(classLoader: LaunchClassLoader){
    NailedTweaker.deobf = try{
      val bytes = classLoader.getClassBytes("net.minecraft.world.World")
      if(bytes == null){
        logger.info("Obfuscated environment detected")
        logger.info("Enabling runtime deobfuscation")
        false
      }else{
        logger.info("Deobfuscated environment detected")
        true
      }
    }catch{
      case e: IOException => false
    }

    classLoader.addClassLoaderExclusion("scala.")
    classLoader.addClassLoaderExclusion("LZMA.")
    classLoader.addClassLoaderExclusion("com.google.common.")
    classLoader.addClassLoaderExclusion("com.nothome.delta.")
    classLoader.addClassLoaderExclusion("org.apache.")
    classLoader.addTransformerExclusion("jk_5.nailed.server.tweaker.transformer.")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.PatchingTransformer")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.EventSubscribtionTransformer")
    if(!NailedTweaker.deobf) classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.RemappingTransformer")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.AccessTransformer")

    AccessTransformer.readConfig("nailed_at.cfg")
  }

  override def getLaunchArguments = new Array[String](0)
  override def getLaunchTarget = "net.minecraft.server.MinecraftServer"
}
