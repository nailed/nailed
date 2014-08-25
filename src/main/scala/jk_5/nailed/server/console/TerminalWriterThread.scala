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

import java.io.{IOException, OutputStream}

import com.google.common.base.Charsets
import com.mojang.util.QueueLogAppender
import jk_5.nailed.server.tweaker.NailedTweaker
import jline.console.ConsoleReader

/**
 * No description given
 *
 * @author jk-5
 */
object TerminalWriterThread extends Thread {

  this.setName("Terminal writer")
  this.setDaemon(true)

  var reader: ConsoleReader = _
  var output: OutputStream = _

  override def run(){
    while(true){
      val msg = QueueLogAppender.getNextLogEvent("TerminalConsole")
      if(msg != null){
        try{
          if(NailedTweaker.useJline){
            reader.print(new StringBuilder(1).append(ConsoleReader.RESET_LINE))
            reader.flush()
            output.write(msg.getBytes(Charsets.UTF_8))
            output.flush()

            try{
              reader.drawLine()
            }catch{
              case e: Exception => reader.getCursorBuffer.clear()
            }
            reader.flush()
          }else{
            output.write(msg.getBytes(Charsets.UTF_8))
            output.flush()
          }
        }catch{
          case e: IOException => e.printStackTrace()
        }
      }
    }
  }
}
