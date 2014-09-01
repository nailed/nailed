package jk_5.nailed.server.network

import com.google.common.collect.BiMap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{EncoderException, MessageToByteEncoder}
import net.minecraft.network.{NetworkManager, Packet, PacketBuffer}

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
object PacketEncoder extends MessageToByteEncoder[Packet] {

  override def encode(ctx: ChannelHandlerContext, msg: Packet, out: ByteBuf){
    val packetId = ctx.channel().attr(NetworkManager.attrKeySendable).get().asInstanceOf[BiMap[java.lang.Integer, Class[_ <: Packet]]].inverse().get(msg.getClass)

    if(packetId == null){
      throw new EncoderException("Can't serialize unregistered packet")
    }

    PacketUtils.writeVarInt(packetId, out)
    msg.writePacketData(new PacketBuffer(out))
  }
}
