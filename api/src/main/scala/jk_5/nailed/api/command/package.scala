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

package jk_5.nailed.api

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
package object command {

  type Command = jk_5.nailed.api.plugin.Command

  @inline def autocomplete(args: Array[String], options: String*): List[String] = {
    val last = args(args.length - 1)
    List(options.filter(_.regionMatches(true, 0, last, 0, last.length)): _*)
  }

  @inline def autocomplete(args: Array[String], options: Iterable[String]): List[String] = {
    val last = args(args.length - 1)
    val ret = mutable.ListBuffer[String]()
    options.filter(_.regionMatches(true, 0, last, 0, last.length)).foreach(o => ret += o)
    ret.toList
  }

  @inline def autocomplete(args: Array[String], options: java.lang.Iterable[String]): List[String] = {
    val last = args(args.length - 1)
    val ret = mutable.ListBuffer[String]()
    val it = options.iterator()
    while(it.hasNext){
      val v  = it.next()
      if(v.regionMatches(true, 0, last, 0, last.length)){
        ret += v
      }
    }
    ret.toList
  }

  def caseInsensitiveMatch(in: String)(m: (String) => Unit) = m(in.toLowerCase)
  def caseInsensitiveMatchWithResult[T](in: String)(m: (String) => T): T = m(in.toLowerCase)

  def getPlayer(sender: CommandSender, target: String): Option[Player] = {
    val p = getPlayers(sender, target)
    if(p.length == 0) None else Some(p(0))
  }

  def getPlayers(sender: CommandSender, pattern: String): Array[Player] = {
    sender match {
      case p: Player => Server.getInstance.getPlayerSelector.matchPlayers(pattern, p.getLocation)
      case _ => throw new CommandException("Only players can use selectors (this is being changed)")
    }
  }

  @inline def autocompleteUsername(args: Array[String]): List[String] = autocomplete(args, Server.getInstance.getOnlinePlayers.map(_.getName))
  @inline def autocompleteUsername(args: Array[String], map: Map): List[String] = autocomplete(args, map.players.map(_.getName))

  def parseInt(sender: CommandSender, input: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): Int = {
    try{
      val int = input.toInt
      if(int > max){
        throw new CommandException("Number " + input + " is bigger than the maximum (" + max + ")")
      }else if(int < min){
        throw new CommandException("Number " + input + " is smaller than the minimum (" + min + ")")
      }else{
        int
      }
    }catch{
      case _: NumberFormatException => throw new CommandException("Entered value " + input + " is not a valid number")
    }
  }

  def parseIntWithDefault(input: String, default: Int): Int = {
    try{
      input.toInt
    }catch{
      case _: NumberFormatException => default
    }
  }

  def senderOrMatches(sender: CommandSender, args: Array[String], index: Int): Array[Player] = sender match {
    case player: Player => Array(player)
    case _ if args.length > index => getPlayers(sender, args(index))
    case _ => throw new NotAPlayerException
  }
}
