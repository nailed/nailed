package jk_5.nailed.server.console

import java.io.ByteArrayOutputStream

import org.apache.logging.log4j.{Level, Logger}

import scala.util.Properties

/**
 * No description given
 *
 * @author jk-5
 */
class LoggerOutputStream(val logger: Logger, val level: Level) extends ByteArrayOutputStream {

  val separator = Properties.lineSeparator

  override def flush(){
    this.synchronized{
      super.flush()
      val record = this.toString
      super.reset()
      if(record.length() > 0 && record != separator){
        logger.log(level, record)
      }
    }
  }
}
