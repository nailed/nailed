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

package jk_5.nailed.server.utils

import java.util
import java.util._
import java.util.regex.Pattern

import jk_5.nailed.api.Server
import jk_5.nailed.api.command._
import jk_5.nailed.api.player.{GameMode, Player}
import jk_5.nailed.api.util.{Location, PlayerSelector}
import jk_5.nailed.api.world.World
import jk_5.nailed.server.player.NailedPlayer

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedPlayerSelector extends PlayerSelector {

  private final val TOKEN_PATTERN = Pattern.compile("^@([parf])(?:\\[([\\w=,!-]*)\\])?$")
  private final val INT_LIST_PATTERN = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)")
  private final val KEY_VALUE_LIST_PATTERN = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)")

  override def matchPlayers(pattern: String, base: Location): Array[Player] = {
    val tokenMatcher = TOKEN_PATTERN.matcher(pattern)
    if(!tokenMatcher.matches){
      val p = Server.getInstance.getPlayerByName(pattern)
      if(p.isEmpty){
        val p2 = Server.getInstance.getPlayer(UUID.fromString(pattern))
        if(p2.isEmpty) return new Array[Player](0) else return Array[Player](p.get)
      }else{
        return Array[Player](p.get)
      }
    }

    val arguments = getArgumentMap(tokenMatcher.group(2)) //map
    val typ = tokenMatcher.group(1)                       //s1
    var minRange = 0         //i
    var maxRange = 0         //j
    var minXP = 0            //k
    var maxXP = Int.MaxValue //l
    var maxCount = if(typ == "a") 0 else 1  //i1
    var gamemode: GameMode = null //j1
    val scoreboardTags = getScoreboardValues(arguments) //map1
    var currentWorldOnly = false
    val location = base.copy()
    var teamName: String = null
    var name: String = null

    if(arguments.contains("rm")){
      minRange = parseIntWithDefault(arguments.get("rm").get, minRange)
      currentWorldOnly = true
    }

    if(arguments.contains("r")){
      maxRange = parseIntWithDefault(arguments.get("r").get, minRange)
      currentWorldOnly = true
    }

    if(arguments.contains("lm")){
      minXP = parseIntWithDefault(arguments.get("lm").get, minXP)
    }

    if(arguments.contains("l")){
      maxXP = parseIntWithDefault(arguments.get("l").get, minXP)
    }

    if(arguments.contains("x")){
      location.setX(parseIntWithDefault(arguments.get("x").get, location.getBlockX))
      currentWorldOnly = true
    }

    if(arguments.contains("y")){
      location.setY(parseIntWithDefault(arguments.get("r").get, location.getBlockY))
      currentWorldOnly = true
    }

    if(arguments.contains("z")){
      location.setZ(parseIntWithDefault(arguments.get("z").get, location.getBlockZ))
      currentWorldOnly = true
    }

    if(arguments.contains("m")){
      gamemode = GameMode.byId(parseIntWithDefault(arguments.get("m").get, if(gamemode == null) Int.MinValue else gamemode.getId))
    }

    if(arguments.contains("c")){
      maxCount = parseIntWithDefault(arguments.get("c").get, maxCount)
    }

    if(arguments.contains("team")){
      teamName = arguments.get("team").get
    }

    if(arguments.contains("name")){
      name = arguments.get("name").get
    }

    val world = if(currentWorldOnly) location.getWorld else null

    typ match {
      case "r" =>
        var l = findPlayers(location, minRange, maxRange, 0, gamemode, minXP, maxXP, scoreboardTags, name, teamName, world)
        Collections.shuffle(l)
        l = l.subList(0, Math.min(maxCount, l.size))
        if(l.isEmpty) new Array[Player](0) else l.toArray(new Array[Player](l.size))
      case "p" | "a" =>
        val l = findPlayers(location, minRange, maxRange, maxCount, gamemode, minXP, maxXP, scoreboardTags, name, teamName, world)
        if(l.isEmpty) new Array[Player](0) else l.toArray(new Array[Player](l.size))
      case _ => new Array[Player](0)
    }
  }

  override def matchesMultiplePlayers(pattern: String): Boolean = {
    val matcher = TOKEN_PATTERN.matcher(pattern)
    if(matcher.matches()){
      val arguments = getArgumentMap(matcher.group(2))
      val typ = matcher.group(1)
      var count = if(typ == "a") 0 else 1
      if(arguments.contains("c")){
        count = parseIntWithDefault(arguments.get("c").get, count)
      }
      count != 1
    }else false
  }

  private def findPlayers(location: Location, minRange: Int, maxRange: Int, maxCount: Int, gamemode: GameMode, minXP: Int, maxXP: Int, scoreboardTags: mutable.Map[String, String], name: String, teamName: String, world: World): util.List[Player] = {
    val players = Server.getInstance.getOnlinePlayers
    if(players.length == 0) return Collections.emptyList()

    val ret = new util.ArrayList[Player]()
    val negativeCount = maxCount < 0               //flag
    val inverseNameMatch = name != null && name.startsWith("!") //flag1
    val inverseTeamNameMatch = teamName != null && name.startsWith("!") //flag2
    val minRangeSq = minRange * minRange //k1
    val maxRangeSq = maxRange * maxRange //l1
    val count = Math.abs(maxCount)       // p_1234_4_

    val playerName = if(inverseNameMatch) name.substring(1) else name
    val playerTeamName = if(inverseTeamNameMatch) teamName.substring(1) else teamName

    val hasMap = world.getMap.isDefined
    val map = world.getMap.orNull

    for(p <- players.map(_.asInstanceOf[NailedPlayer])){
      var continue = false
      //If world is not null, check if the player has the same world. Else check if the player has the same map
      //Then check if the player's name matches the queried player name
      if(((world == null && hasMap && p.world.getMap.isDefined && p.world.getMap.get == map) || p.world == world) && (playerName == null || inverseNameMatch != playerName.equalsIgnoreCase(p.getName))){
        if(playerTeamName != null){
          val team = p.getEntity.getTeam
          val teamName = if(team == null) "" else team.getRegisteredName
          if(inverseTeamNameMatch == playerTeamName.equalsIgnoreCase(teamName)){
            continue = true
          }
        }
        if(!continue && location != null && (minRange > 0 || maxRange > 0)){
          val dist = location.distanceSquared(p.getLocation)
          if((minRange > 0 && dist < minRangeSq) || (maxRange > 0 && dist > maxRangeSq)){
            continue = true
          }
        }
        if(!continue){
          //TODO: scoreboard matching! see func_96457
          if(gamemode == null || gamemode == p.getGameMode){
            if((minXP <= 0 || p.getEntity.experienceLevel >= minXP) && p.getEntity.experienceLevel <= maxXP){
              ret.add(p)
            }
          }
        }
      }
    }

    Collections.sort(ret, new Comparator[Player] {
      override def compare(o1: Player, o2: Player): Int = {
        val dist1 = o1.asInstanceOf[NailedPlayer].getEntity.getDistanceSq(location.getX, location.getY, location.getZ)
        val dist2 = o2.asInstanceOf[NailedPlayer].getEntity.getDistanceSq(location.getX, location.getY, location.getZ)
        if(dist1 < dist2) -1 else{if(dist1 > dist2) 1 else 0}
      }
    })

    if(negativeCount){
      Collections.reverse(ret)
    }

    if(count > 0){
      ret.subList(0, Math.min(count, ret.size()))
    }else ret
  }

  private def getScoreboardValues(input: mutable.Map[String, String]): mutable.Map[String, String] = {
    val ret = mutable.Map[String, String]()
    for(s <- input.keys){
      if(s.startsWith("score_") && s.length > 6){
        val key = s.substring(6)
        ret.put(key, parseIntWithDefault(input.get(s).get, 1).toString)
      }
    }
    ret
  }

  private def getArgumentMap(input: String): mutable.Map[String, String] = {
    if(input == null) return mutable.Map()
    val matcher = INT_LIST_PATTERN.matcher(input)
    val ret = mutable.Map[String, String]()
    var coordIndex = 0
    var i = -1
    while(matcher.find()){
      val key = coordIndex match {
        case 0 => "x"
        case 1 => "y"
        case 2 => "z"
        case 3 => "r"
        case _ => null
      }
      coordIndex += 1
      val value = matcher.group(1)
      if(key != null && value.length > 0){
        ret.put(key, value)
      }

      i = matcher.end()
    }

    if(i < input.length){
      val m2 = KEY_VALUE_LIST_PATTERN.matcher(if(i == -1) input else input.substring(i))
      while(m2.find()){
        ret.put(matcher.group(1), matcher.group(2))
      }
    }
    ret
  }
}
