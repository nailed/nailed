package jk_5.nailed.server.tweaker

import java.io.{File, IOException, PrintStream}
import java.util

import jk_5.nailed.server.console.{LoggerOutputStream, TerminalWriterThread}
import jk_5.nailed.server.tweaker.patcher.BinPatchManager
import jk_5.nailed.server.tweaker.remapping.NameRemapper
import jk_5.nailed.server.tweaker.transformer.AccessTransformer
import jline.console.ConsoleReader
import jline.{TerminalFactory, UnsupportedTerminal}
import joptsimple.{OptionException, OptionParser}
import net.minecraft.launchwrapper.{ITweaker, Launch, LaunchClassLoader}
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.{Level, LogManager}

import scala.collection.convert.wrapAsScala._
import scala.util.Properties

/**
 * No description given
 *
 * @author jk-5
 */
object NailedTweaker {
  var gameDir: File = _
  var deobf = false
  var useJline = true
  var useConsole = true
  var consoleReader: ConsoleReader = null
  lazy val classLoader = Launch.classLoader
}

class NailedTweaker extends ITweaker {

  val logger = LogManager.getLogger

  override def acceptOptions(args: util.List[String], gameDir: File, assetsDir: File, profile: String){
    //Step 1 - Parse command line arguments
    val parser = new OptionParser
    parser.allowsUnrecognizedOptions()

    parser.accepts("nojline", "Disables jline and emulates the vanilla console")
    parser.accepts("noconsole", "Disables the console")

    val options = try{
      parser.parse(args.toArray(new Array[String](args.size())): _*)
    }catch{
      case e: OptionException =>
        logger.fatal("Error while parsing arguments: " + e.getLocalizedMessage)
        System.exit(1)
        null
    }

    NailedTweaker.useJline = Properties.propOrEmpty("jline.terminal") != "jline.UnsupportedTerminal"

    if(options.has("nojline")){
      Properties.setProp("user.language", "en")
      NailedTweaker.useJline = false
    }

    if(!NailedTweaker.useJline){
      Properties.setProp(TerminalFactory.JLINE_TERMINAL, classOf[UnsupportedTerminal].getName)
    }

    if(options.has("noconsole")){
      NailedTweaker.useConsole = false
    }

    //Step 2 - Initialize logging

    //This is needed, because the QueueLogAppender is initialized in another classloader than we are in, so the static values are not accessible
    //With this hack, i get the static queue map from the parent classloader and hack it into this one
    val logQueue = {
      val loader = classOf[LaunchClassLoader].getClassLoader
      val cl = loader.loadClass("com.mojang.util.QueueLogAppender")
      val f = cl.getDeclaredField("QUEUES")
      f.setAccessible(true)
      val q = f.get(null).asInstanceOf[util.Map[String, util.concurrent.BlockingQueue[String]]]
      q.get("TerminalConsole")
    }

    //Are we running in a terminal? If we are not, disable jline
    if(System.console() == null){
      Properties.setProp("jline.terminal", "jline.UnsupportedTerminal")
      NailedTweaker.useJline = false
    }

    try{
      NailedTweaker.consoleReader = new ConsoleReader(System.in, System.out)
      NailedTweaker.consoleReader.setExpandEvents(false) // Avoid parsing exceptions for uncommonly used event designators
    }catch{
      case e: Throwable =>
        try{
          //Try again with jline disabled for Windows users without C++ 2008 Redistributable
          Properties.setProp("jline.terminal", "jline.UnsupportedTerminal")
          Properties.setProp("user.language", "en")
          NailedTweaker.useJline = false
          NailedTweaker.consoleReader = new ConsoleReader(System.in, System.out)
          NailedTweaker.consoleReader.setExpandEvents(false)
        }catch{
          case e: IOException => this.logger.error("Error while initializing jline", e)
        }
    }

    val l = LogManager.getRootLogger.asInstanceOf[org.apache.logging.log4j.core.Logger]
    for(appender <- l.getAppenders.values()){
      if(appender.isInstanceOf[ConsoleAppender]){
        l.removeAppender(appender)
      }
    }

    TerminalWriterThread.output = System.out
    TerminalWriterThread.reader = NailedTweaker.consoleReader
    TerminalWriterThread.queue = logQueue
    TerminalWriterThread.start()

    System.setOut(new PrintStream(new LoggerOutputStream(l, Level.INFO), true))
    System.setErr(new PrintStream(new LoggerOutputStream(l, Level.WARN), true))

    //Step 3 - Read configuration
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
