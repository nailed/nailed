package jk_5.nailed.internalplugin.command

import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Command
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.network.play.server.S07PacketRespawn
import net.minecraft.world.WorldSettings.GameType
import net.minecraft.world.{EnumDifficulty, WorldType}

/**
 * No description given
 *
 * @author jk-5
 */
object CommandGoto extends Command("goto") {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case p: Player =>
        p.teleportTo(Integer.parseInt(args(0)))
        val e = p.asInstanceOf[NailedPlayer].getEntity
        e.playerNetServerHandler.sendPacket(new S07PacketRespawn(-1, EnumDifficulty.PEACEFUL, WorldType.DEFAULT, GameType.CREATIVE))
        e.playerNetServerHandler.sendPacket(new S07PacketRespawn(0, EnumDifficulty.PEACEFUL, WorldType.DEFAULT, GameType.CREATIVE))
      case _ =>
    }
  }
}
