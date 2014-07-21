package jk_5.nailed.api.team

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
trait Team {

  def id: String
  def name: String
  def color: ChatColor

  def members: Array[Player]
  def onPlayerJoined(player: Player)
  def onPlayerLeft(player: Player)
}
