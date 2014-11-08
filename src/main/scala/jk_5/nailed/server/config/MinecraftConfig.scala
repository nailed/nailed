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
import net.minecraft.server.dedicated.PropertyManager
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class MinecraftConfig extends PropertyManager(null) {

  val logger = LogManager.getLogger
  val file = new File("settings.conf")

  private var config: Config = _

  logger.info("Loading minecraft config")

  if(!file.exists() || file.length() == 0){
    val in = Channels.newChannel(this.getClass.getResourceAsStream("/reference.conf"))
    val out = new FileOutputStream(file).getChannel
    out.transferFrom(in, 0, Long.MaxValue)
    in.close()
    out.close()
  }
  val defaults = ConfigFactory.defaultReference().withOnlyPath("minecraft")
  val conf = ConfigFactory.parseFile(file).withFallback(defaults)
  try{
    config = conf.getConfig("minecraft")
  }catch{
    case e: Throwable =>
      logger.warn("Failed to load minecraft config, using defaults.", e)
      config = defaults.getConfig("minecraft")
  }

  override def saveProperties(){
    logger.debug("Attempted to save minecraft config")
  }

  private def remap(key: String): String = key match {
    case "enable-query" => "query.enabled"
    case "enable-rcon" => "rcon.enabled"
    case k => k
  }

  override def getPropertiesFile = null
  override def getStringProperty(key: String, default: String) = if(config.hasPath(remap(key))) config.getString(remap(key)) else{
    logger.warn(s"Attempted to get minecraft config value \'${remap(key)}\', but it was not found in the configuration. Returning default value (\'$default\')")
    default
  }
  override def getIntProperty(key: String, default: Int) = if(config.hasPath(remap(key))) config.getInt(remap(key)) else{
    logger.warn(s"Attempted to get minecraft config value \'${remap(key)}\', but it was not found in the configuration. Returning default value (\'$default\')")
    default
  }
  override def getBooleanProperty(key: String, default: Boolean) = if(config.hasPath(remap(key))) config.getBoolean(remap(key)) else{
    logger.warn(s"Attempted to get minecraft config value \'${remap(key)}\', but it was not found in the configuration. Returning default value (\'$default\')")
    default
  }
  override def getLongProperty(key: String, default: Long) = if(config.hasPath(remap(key))) config.getLong(remap(key)) else{
    logger.warn(s"Attempted to get minecraft config value \'${remap(key)}\', but it was not found in the configuration. Returning default value (\'$default\')")
    default
  }
  override def setProperty(key: String, value: scala.Any){}
  override def generateNewProperties(){}
}
