package jk_5.nailed.server.console

import java.util

import jk_5.nailed.api.Server
import jk_5.nailed.server.NailedEventFactory
import jline.console.completer.Completer
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object Autocompleter extends Completer {

  val logger = LogManager.getLogger

  override def complete(buffer: String, cursor: Int, candidates: util.List[CharSequence]): Int = {
    try{
      val results = mutable.ListBuffer[String]()
      logger.info(Server.getInstance)
      logger.info(Server.getInstance.getPluginManager)
      logger.info(NailedEventFactory.serverCommandSender)
      logger.info(buffer)
      logger.info(candidates)
      Server.getInstance.getPluginManager.dispatchCommand(NailedEventFactory.serverCommandSender, buffer, results)
      if(results.isEmpty) return cursor
      candidates.addAll(results)

      val lastSpace = buffer.lastIndexOf(' ')
      if(lastSpace == -1){
        cursor - buffer.length
      }else{
        cursor - (buffer.length - lastSpace - 1)
      }
    }catch{
      case e: Exception =>
        logger.error("An exception has occurred during tab completion", e)
        cursor
    }
  }
}
