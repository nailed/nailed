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

  def caseInsensitiveMatch(in: String)(m: (String) => Unit) = m(in.toLowerCase)

  //TODO: proper player selector
  def getTargetPlayer(sender: CommandSender, target: String): Option[Player] = {
    val pl = Server.getInstance.getPlayerByName(target)
    if(pl.isEmpty) throw new PlayerNotFoundException(target)
    pl
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

  def senderPlayerOrArgument(sender: CommandSender, args: Array[String], index: Int): Player = sender match {
    case player: Player => player
    case _ =>
      if(args.length > index){
        val p = Server.getInstance.getPlayerByName(args(index))
        if(p.isDefined) p.get else throw new PlayerNotFoundException(args(index))
      }else{
        throw new CommandException("You are not a player")
      }
  }
}
