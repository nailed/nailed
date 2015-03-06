package jk_5.nailed.server.teamspeak

import java.util

import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.teamspeak.api.JTS3ServerQuery
import org.apache.logging.log4j.LogManager

object TeamspeakManager {

  final val config = NailedPlatform.config.getConfig("teamspeak")
  final val enabled = config.getBoolean("enabled")
  final val host = config.getString("host")
  final val port = config.getInt("port")
  final val username = config.getString("username")
  final val password = config.getString("password")
  var connected = false
  private final val logger = LogManager.getLogger
  private final val server = new JTS3ServerQuery

  def start(){
    if(!enabled){
      logger.info("Teamspeak integration is disabled in the config")
      return
    }
    logger.info("Starting teamspeak integration")
    if(!this.server.connectTS3Query(this.host, this.port)){
      displayError()
    }
  }

  private def displayError(){
    val error: String = this.server.getLastError
    if (error != null) {
      System.out.println("Teamspeak error:")
      System.out.println(error)
      if (this.server.getLastErrorPermissionID != -1) {
        val permInfo: util.HashMap[String, String] = this.server.getPermissionInfo(this.server.getLastErrorPermissionID)
        if (permInfo != null) {
          System.out.println("Missing Permission: " + permInfo.get("permname"))
        }
      }
    }
  }
}
