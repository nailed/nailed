package jk_5.nailed.server.console

import java.io.{IOException, OutputStream}

import com.google.common.base.Charsets
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
  var queue: java.util.concurrent.BlockingQueue[String] = _

  override def run(){
    queue.clear()
    while(true){
      val msg = queue.take()
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
