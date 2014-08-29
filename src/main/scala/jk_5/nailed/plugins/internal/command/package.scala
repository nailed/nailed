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

package jk_5.nailed.plugins.internal

import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player

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

  def caseInsensitiveMatch(in: String)(m: (String) => Unit) = m(in.toLowerCase)

  //TODO: proper player selector
  def getTargetPlayer(sender: CommandSender, target: String): Option[Player] = {
    val pl = Server.getInstance.getPlayerByName(target)
    if(pl.isEmpty) sender.sendMessage(new ComponentBuilder("Player not found").color(ChatColor.RED).create())
    pl
  }

  @inline def getUsernameOptions(args: Array[String]): List[String] = getOptions(args, Server.getInstance.getOnlinePlayers.map(_.getName))
  @inline def getUsernameOptions(args: Array[String], map: Map): List[String] = getOptions(args, map.players.map(_.getName))

  def parseInt(sender: CommandSender, input: String, min: Int = Int.MinValue, max: Int = Int.MaxValue): Option[Int] = {
    try{
      val int = input.toInt
      if(int > max){
        sender.sendMessage(new ComponentBuilder("Number " + input + " is bigger than the maximum (" + max + ")").color(ChatColor.RED).create())
        None
      }else if(int < min){
        sender.sendMessage(new ComponentBuilder("Number " + input + " is smaller than the minimum (" + min + ")").color(ChatColor.RED).create())
        None
      }else{
        Some(int)
      }
    }catch{
      case _: NumberFormatException =>
        sender.sendMessage(new ComponentBuilder("Entered value " + input + " is not a valid number").color(ChatColor.RED).create())
        None
    }
  }
}
