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

package jk_5.nailed.server.config

import java.io.{File, FileOutputStream}
import java.nio.channels.Channels

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */

object Settings {

  private var config: Config = _
  private val logger = LogManager.getLogger

  def load() = {
    val file = new File("settings.conf")
    logger.info("Loading config")
    if(!file.exists() || file.length() == 0){
      val in = Channels.newChannel(Settings.getClass.getResourceAsStream("/reference.conf"))
      val out = new FileOutputStream(file).getChannel
      out.transferFrom(in, 0, Long.MaxValue)
      in.close()
      out.close()
    }
    val defaults = ConfigFactory.defaultReference().withOnlyPath("nailed")
    val conf = ConfigFactory.parseFile(file).withFallback(defaults)
    try{
      config = conf.getConfig("nailed")
    }catch{
      case e: Throwable =>
        logger.warn("Failed to load nailed config, using defaults.", e)
        config = defaults.getConfig("nailed")
    }
    config
  }
}
