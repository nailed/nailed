package jk_5.nailed.api.map

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.scoreboard.ScoreboardManager
import jk_5.nailed.api.team.Team
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
trait Map {

  def id: Int
  def worlds: Array[World]
  def mappack: Mappack
  def addWorld(world: World)

  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)

  def getTeam(team: String): Option[Team]
  def getPlayerTeam(player: Player): Team
  def setPlayerTeam(player: Player, team: Team)
  def getTeams: Array[Team]

  def broadcastChatMessage(message: BaseComponent)
  def broadcastChatMessage(message: BaseComponent*)
  def broadcastChatMessage(message: Array[BaseComponent])

  def players: Array[Player]

  def getScoreboardManager: ScoreboardManager
}
