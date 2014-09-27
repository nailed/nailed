package jk_5.nailed.worldedit

import com.sk89q.worldedit.session.SessionKey
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
case class DefaultSessionKey(player: Player) extends SessionKey {
  override def getUniqueId = player.getUniqueId
  override def getName = player.getName
  override def isActive = player.isOnline
  override def isPersistent = true
}
