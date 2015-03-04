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

package jk_5.nailed.server.world

import java.util

import jk_5.nailed.api.gamerule.{DefaultGameRules, EditableGameRules}
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.mappack.metadata.MappackWorld
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.world._
import jk_5.nailed.server.map.gamerule.WrappedEditableGameRules
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.network.play.server.S41PacketServerDifficulty
import net.minecraft.world.{EnumDifficulty, WorldServer}

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedWorld(var wrapped: WorldServer, val context: WorldContext = null) extends World {

  private var map: Option[Map] = None
  private val playerSet = mutable.HashSet[Player]()
  private var players = new Array[Player](0)

  private val provider: Option[WorldProvider] = wrapped.provider match {
    case p: DelegatingWorldProvider => Some(p.wrapped)
    case _ => None
  }

  this.getConfig match {
    case c: MappackWorld => setDifficulty(c.difficulty())
    case _ =>
  }

  override val getGameRules: EditableGameRules = if(this.context != null && this.context.getConfig != null) new WrappedEditableGameRules(context.getConfig.gameRules) else new WrappedEditableGameRules(DefaultGameRules.INSTANCE)

  override def getDimensionId = wrapped.provider.getDimensionId
  override def getName = "world_" + getDimensionId
  override def getPlayers: util.Collection[Player] = util.Arrays.asList(players: _*)
  override def getDimension = this.provider match {
    case Some(p) => p.getDimension
    case None => Dimension.OVERWORLD
  }

  override def setMap(map: Map) = this.map = Some(map)
  override def getMap = this.map.orNull
  override def getConfig: MappackWorld = if(this.context == null) null else this.context.getConfig

  override def onPlayerJoined(player: Player){
    println("Player " + player.toString + " joined world " + this.toString)
    playerSet += player
    players = playerSet.toArray
    player.asInstanceOf[NailedPlayer].sendPacket(new S41PacketServerDifficulty(wrapped.getDifficulty, false))
    if(this.getConfig.resourcePackUrl() != null){
      player.loadResourcePack(this.getConfig.resourcePackUrl(), "") //TODO: fix hash
    }
  }

  override def onPlayerLeft(player: Player){
    println("Player " + player.toString + " left world " + this.toString)
    playerSet -= player
    players = playerSet.toArray
  }

  override def getTime = this.wrapped.getWorldTime.toInt
  override def setTime(time: Int) = this.wrapped.setWorldTime(time)

  override def getWeather: WeatherType = {
    val rain = this.wrapped.isRaining
    val thunder = this.wrapped.isThundering

    if(!rain && !thunder) WeatherType.DRY
    else if(!thunder) WeatherType.RAIN
    else WeatherType.THUNDER
  }
  override def setWeather(weather: WeatherType) = weather match {
    case WeatherType.DRY =>
      this.wrapped.getWorldInfo.setRaining(false)
      this.wrapped.getWorldInfo.setRainTime(0)
      this.wrapped.getWorldInfo.setThundering(false)
      this.wrapped.getWorldInfo.setThunderTime(0)
    case WeatherType.RAIN =>
      this.wrapped.getWorldInfo.setRaining(true)
      this.wrapped.getWorldInfo.setThundering(false)
      this.wrapped.getWorldInfo.setThunderTime(0)
    case WeatherType.THUNDER =>
      this.wrapped.getWorldInfo.setRaining(true)
      this.wrapped.getWorldInfo.setThundering(true)
  }

  override def getDifficulty = Difficulty.byId(wrapped.getDifficulty.getDifficultyId)
  override def setDifficulty(difficulty: Difficulty){
    val diff = EnumDifficulty.getDifficultyEnum(difficulty.getId)
    this.wrapped.getWorldInfo.setDifficulty(diff)
    this.players.map(_.asInstanceOf[NailedPlayer]).foreach(_.sendPacket(new S41PacketServerDifficulty(diff, false)))
    difficulty match {
      case Difficulty.HARD | Difficulty.NORMAL | Difficulty.EASY => wrapped.setAllowedSpawnTypes(true, true)
      case Difficulty.PEACEFUL => wrapped.setAllowedSpawnTypes(false, true)
    }
  }

  override def toString = s"NailedWorld{id=$getDimensionId,name=$getName,dimension=$getDimension,gameRules=$getGameRules}"
}
