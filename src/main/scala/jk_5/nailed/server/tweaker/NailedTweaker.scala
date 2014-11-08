/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.tweaker

import java.io.{File, IOException, PrintStream}
import java.util

import io.netty.util.internal.logging.InternalLoggerFactory
import jk_5.nailed.server.logging.LoggerOutputStream
import jk_5.nailed.server.tweaker.patcher.BinPatchManager
import jk_5.nailed.server.tweaker.remapping.NameRemapper
import jk_5.nailed.server.tweaker.transformer.AccessTransformer
import joptsimple.{OptionException, OptionParser}
import net.minecraft.launchwrapper.{ITweaker, Launch, LaunchClassLoader}
import org.apache.logging.log4j.{Level, LogManager}

/**
 * No description given
 *
 * @author jk-5
 */
object NailedTweaker {
  var gameDir: File = _
  var deobf = false
  var acceptEula = true
  lazy val classLoader = Launch.classLoader
}

class NailedTweaker extends ITweaker {

  val logger = LogManager.getLogger

  override def acceptOptions(args: util.List[String], gameDir: File, assetsDir: File, profile: String){
    //Step 1 - Parse command line arguments
    val parser = new OptionParser
    parser.allowsUnrecognizedOptions()

    parser.accepts("accept-eula", "Accept the EULA, so you don't need to change the eula.txt file")

    val options = try{
      parser.parse(args.toArray(new Array[String](args.size())): _*)
    }catch{
      case e: OptionException =>
        logger.fatal("Error while parsing arguments: " + e.getLocalizedMessage)
        System.exit(1)
        null
    }

    if(options.has("accept-eula")){
      NailedTweaker.acceptEula = true
    }

    //Step 2 - Read configuration
    NailedTweaker.gameDir = gameDir
    NailedVersion.readConfig()

    logger.info(s"Initializing Nailed version ${NailedVersion.full}")
    if(NailedVersion.isSnapshot){
      logger.info("This is a snapshot version. It might be instable/buggy")
    }

    BinPatchManager.setup()
    NameRemapper.init()

    InternalLoggerFactory.getInstance("INITLOGGER") //Force netty's logger to initialize
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
    classLoader.addClassLoaderExclusion("com.google.")
    classLoader.addClassLoaderExclusion("com.nothome.delta.")
    classLoader.addClassLoaderExclusion("org.apache.")
    classLoader.addClassLoaderExclusion("com.mojang.")
    classLoader.addClassLoaderExclusion("org.fusesource.")
    classLoader.addTransformerExclusion("jk_5.nailed.server.tweaker.mixin.")
    classLoader.addTransformerExclusion("jk_5.nailed.server.tweaker.transformer.")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.PatchingTransformer")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.EventSubscribtionTransformer")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.mixin.MixinTransformer")
    if(!NailedTweaker.deobf) classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.RemappingTransformer")
    classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.AccessTransformer")

    AccessTransformer.readConfig("nailed_at.cfg")

    // Step 3 - Initialize logging
    System.setOut(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSOUT"), Level.INFO), true))
    System.setErr(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSERR"), Level.WARN), true))
  }

  override def getLaunchArguments = new Array[String](0)
  override def getLaunchTarget = "net.minecraft.server.MinecraftServer"
}
