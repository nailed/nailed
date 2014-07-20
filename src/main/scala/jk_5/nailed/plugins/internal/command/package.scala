package jk_5.nailed.plugins.internal

import jk_5.nailed.api.Server

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
package object command {

  @inline def getOptions(args: Array[String], options: String*): List[String] = {
    val last = args(args.length - 1)
    List(options.filter(_.regionMatches(true, 0, last, 0, last.length)): _*)
  }

  @inline def getOptions(args: Array[String], options: Iterable[String]): List[String] = {
    val last = args(args.length - 1)
    val ret = mutable.ListBuffer[String]()
    options.filter(_.regionMatches(true, 0, last, 0, last.length)).foreach(o => ret += o)
    ret.toList
  }

  @inline def getUsernameOptions(args: Array[String]): List[String] = getOptions(args, Server.getInstance.getOnlinePlayers.map(_.getName))
  //@inline def getUsernameOptions(args: Array[String], map: Map): List[String] = getOptions(args, map.getPlayers.map(_.getUsername))
}
