package jk_5.nailed.server.network

import java.io.IOException
import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.minecraft.network._

/**
 * No description given
 *
 * @author jk-5
 */
class PacketDecoder extends ByteToMessageDecoder {

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]){
    val length = in.readableBytes()

    if(length != 0){
      val id = PacketUtils.readVarInt(in)
      val connectionState = ctx.channel().attr(NetworkManager.attrKeyConnectionState).get().asInstanceOf[EnumConnectionState]
      val packet = connectionState.getPacket(EnumPacketDirection.SERVERBOUND, id)

      if(packet == null) throw new IOException("Bad packet id " + id)
      packet.readPacketData(new PacketBuffer(in))
      if(in.readableBytes() > 0) throw new IOException("Packet " + connectionState.getId + "/" + id + " (" + packet.getClass.getSimpleName + ") was larger than expected. Found " + in.readableBytes() + " bytes extra")
      out.add(packet)
    }
  }
}
