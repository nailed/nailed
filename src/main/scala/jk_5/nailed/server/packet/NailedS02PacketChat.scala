package jk_5.nailed.server.packet

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.chat.serialization.ComponentSerializer
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.server.S02PacketChat

/**
 * No description given
 *
 * @author jk-5
 */
class NailedS02PacketChat(val comp: BaseComponent*) extends S02PacketChat {

  override def writePacketData(packet: PacketBuffer){
    packet.writeStringToBuffer(ComponentSerializer.toString(this.comp: _*))
  }

  override def serialize(): String = "component='" + comp.toString + "'"
}
