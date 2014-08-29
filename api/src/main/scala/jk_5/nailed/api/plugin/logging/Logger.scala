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

package jk_5.nailed.api.plugin.logging

import jk_5.nailed.api.plugin.Plugin
import org.apache.logging.log4j.{LogManager, ThreadContext, Level => L4JLevel}

/**
 * No description given
 *
 * @author jk-5
 */
class Logger(private final val plugin: Plugin) {

  private final val logger = LogManager.getLogger("plugin:" + plugin.getDescription.getName)
  private final val l4jToInternal = Map(
    L4JLevel.OFF -> Level.OFF,
    L4JLevel.FATAL -> Level.FATAL,
    L4JLevel.ERROR -> Level.ERROR,
    L4JLevel.WARN -> Level.WARN,
    L4JLevel.INFO -> Level.INFO,
    L4JLevel.DEBUG -> Level.DEBUG,
    L4JLevel.TRACE -> Level.TRACE,
    L4JLevel.ALL -> Level.ALL
  )

  private def before(){
    ThreadContext.put("plugin", this.plugin.getDescription.getName)
  }

  private def after(){
    ThreadContext.remove("plugin")
  }



  def catching(level: Level, t: Throwable){
    before()
    logger.catching(level.getL4JLevel, t)
    after()
  }
  def catching(t: Throwable){
    before()
    logger.catching(t)
    after()
  }
  def debug(message: scala.Any){
    before()
    logger.debug(message)
    after()
  }
  def debug(message: scala.Any, t: Throwable){
    before()
    logger.debug(message, t)
    after()
  }
  def debug(message: String){
    before()
    logger.debug(message)
    after()
  }
  def debug(message: String, params: AnyRef*): Unit = {
    before()
    logger.debug(message, params)
    after()
  }
  def debug(message: String, t: Throwable): Unit = {
    before()
    logger.debug(message, t)
    after()
  }
  def entry(): Unit = {
    before()
    logger.entry()
    after()
  }
  def entry(params: AnyRef*): Unit = {
    before()
    logger.entry(params)
    after()
  }
  def error(message: scala.Any): Unit = {
    before()
    logger.error(message)
    after()
  }
  def error(message: scala.Any, t: Throwable): Unit = {
    before()
    logger.error(message, t)
    after()
  }
  def error(message: String): Unit = {
    before()
    logger.error(message)
    after()
  }
  def error(message: String, params: AnyRef*): Unit = {
    before()
    logger.error(message, params)
    after()
  }
  def error(message: String, t: Throwable): Unit = {
    before()
    logger.error(message, t)
    after()
  }
  def exit(): Unit = {
    before()
    logger.exit()
    after()
  }
  def exit[R](result: R): R = {
    before()
    val r = logger.exit(result)
    after()
    r
  }
  def fatal(message: scala.Any): Unit = {
    before()
    logger.fatal(message)
    after()
  }
  def fatal(message: scala.Any, t: Throwable): Unit = {
    before()
    logger.fatal(message, t)
    after()
  }
  def fatal(message: String): Unit = {
    before()
    logger.fatal(message)
    after()
  }
  def fatal(message: String, params: AnyRef*): Unit = {
    before()
    logger.fatal(message, params)
    after()
  }
  def fatal(message: String, t: Throwable): Unit = {
    before()
    logger.fatal(message, t)
    after()
  }
  def getLevel: Level = {
    before()
    val r = l4jToInternal.get(logger.getLevel).orNull
    after()
    r
  }
  def getName: String = {
    before()
    val r = logger.getName
    after()
    r
  }
  def info(message: scala.Any): Unit = {
    before()
    logger.info(message)
    after()
  }
  def info(message: scala.Any, t: Throwable): Unit = {
    before()
    logger.info(message, t)
    after()
  }
  def info(message: String): Unit = {
    before()
    logger.info(message)
    after()
  }
  def info(message: String, params: AnyRef*): Unit = {
    before()
    logger.info(message, params)
    after()
  }
  def info(message: String, t: Throwable): Unit = {
    before()
    logger.info(message, t)
    after()
  }
  def isDebugEnabled: Boolean = {
    before()
    val r = logger.isDebugEnabled
    after()
    r
  }
  def isEnabled(level: Level): Boolean = {
    before()
    val r = logger.isEnabled(level.getL4JLevel)
    after()
    r
  }
  def isErrorEnabled: Boolean = {
    before()
    val r = logger.isErrorEnabled
    after()
    r
  }
  def isFatalEnabled: Boolean = {
    before()
    val r = logger.isFatalEnabled
    after()
    r
  }
  def isInfoEnabled: Boolean = {
    before()
    val r = logger.isInfoEnabled
    after()
    r
  }
  def isTraceEnabled: Boolean = {
    before()
    val r = logger.isTraceEnabled
    after()
    r
  }
  def isWarnEnabled: Boolean = {
    before()
    val r = logger.isWarnEnabled
    after()
    r
  }
  def log(level: Level, message: scala.Any): Unit = {
    before()
    logger.log(level.getL4JLevel, message)
    after()
  }
  def log(level: Level, message: scala.Any, t: Throwable): Unit = {
    before()
    logger.log(level.getL4JLevel, message, t)
    after()
  }
  def log(level: Level, message: String): Unit = {
    before()
    logger.log(level.getL4JLevel, message)
    after()
  }
  def log(level: Level, message: String, params: AnyRef*): Unit = {
    before()
    logger.log(level.getL4JLevel, message, params)
    after()
  }
  def log(level: Level, message: String, t: Throwable): Unit = {
    before()
    logger.log(level.getL4JLevel, message, t)
    after()
  }
  def printf(level: Level, format: String, params: AnyRef*): Unit = {
    before()
    logger.printf(level.getL4JLevel, format, params)
    after()
  }
  def throwing[T <: Throwable](level: Level, t: T): T = {
    before()
    val r = logger.throwing(level.getL4JLevel, t)
    after()
    r
  }
  def throwing[T <: Throwable](t: T): T = {
    before()
    val r = logger.throwing(t)
    after()
    r
  }
  def trace(message: scala.Any): Unit = {
    before()
    logger.trace(message)
    after()
  }
  def trace(message: scala.Any, t: Throwable): Unit = {
    before()
    logger.trace(message, t)
    after()
  }
  def trace(message: String): Unit = {
    before()
    logger.trace(message)
    after()
  }
  def trace(message: String, params: AnyRef*): Unit = {
    before()
    logger.trace(message, params)
    after()
  }
  def trace(message: String, t: Throwable): Unit = {
    before()
    logger.trace(message, t)
    after()
  }
  def warn(message: scala.Any): Unit = {
    before()
    logger.warn(message)
    after()
  }
  def warn(message: scala.Any, t: Throwable): Unit = {
    before()
    logger.warn(message, t)
    after()
  }
  def warn(message: String): Unit = {
    before()
    logger.warn(message)
    after()
  }
  def warn(message: String, params: AnyRef*){
    before()
    logger.warn(message, params)
    after()
  }
  def warn(message: String, t: Throwable){
    before()
    logger.warn(message, t)
    after()
  }
}
