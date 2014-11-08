package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.api.world.{Difficulty, WeatherType}
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld

import scala.collection.convert.wrapAsScala._

/**
  * No description given
  *
  * @author jk-5
  */
class ScriptWorldApi(private[this] val world: NailedWorld) {

  def getPlayers: Array[ScriptPlayerApi] = {
    world.getPlayers.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer])).toArray
  }

  def setTime(time: Int) = world.setTime(time)
  def setWeather(weather: WeatherType) = world.setWeather(weather)
  def setDifficulty(difficulty: Difficulty) = world.setDifficulty(difficulty)
}
