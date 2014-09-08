package jk_5.nailed.server.map.game.script

import jk_5.nailed.api.world.WeatherType
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld

/**
  * No description given
  *
  * @author jk-5
  */
class ScriptWorldApi(private val world: NailedWorld) {

  def getPlayers: Array[ScriptPlayerApi] = {
    world.getPlayers.map(p => new ScriptPlayerApi(p.asInstanceOf[NailedPlayer]))
  }

  def setTime(time: Int) = world.setTime(time)
  def setWeather(weather: WeatherType) = world.setWeather(weather)
}
