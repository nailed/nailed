package jk_5.nailed.server.scoreboard

import jk_5.nailed.api.scoreboard.ScoreboardManager
import net.minecraft.network.Packet

/**
 * No description given
 *
 * @author jk-5
 */
trait NetworkedScoreboardManager extends ScoreboardManager {

  def sendPacket(packet: Packet)
}
