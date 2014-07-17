package jk_5.nailed.server.console

import java.io.IOException

import jk_5.nailed.server.tweaker.NailedTweaker
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.appender.ConsoleAppender

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandReaderThread extends Thread {

  this.setName("Console reader")
  this.setDaemon(true)

  private val logger = LogManager.getLogger

  override def run(){
    val l = LogManager.getRootLogger.asInstanceOf[org.apache.logging.log4j.core.Logger]
    for(appender <- l.getAppenders.values()){
      if(appender.isInstanceOf[ConsoleAppender]){
        l.removeAppender(appender)
      }
    }

    if(!NailedTweaker.useConsole) return
    val server = MinecraftServer.getServer.asInstanceOf[DedicatedServer]
    val reader = NailedTweaker.consoleReader
    try{
      while(!server.isServerStopped && server.isServerRunning){
        val s = if(NailedTweaker.useJline){
          reader.readLine(">", null)
        }else{
          reader.readLine()
        }
        if(s != null){
          server.addPendingCommand(s, server)
        }
      }
    }catch{
      case e: IOException => logger.error("Error while reading console input", e)
    }
  }
}
