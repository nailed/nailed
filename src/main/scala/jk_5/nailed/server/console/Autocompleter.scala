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

import java.util

import jline.console.completer.Completer
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object Autocompleter extends Completer {

  val logger = LogManager.getLogger

  override def complete(buffer: String, cursor: Int, candidates: util.List[CharSequence]): Int = {
    /*try{
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
    }*/
    cursor
  }
}
