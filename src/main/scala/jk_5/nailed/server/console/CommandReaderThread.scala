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
