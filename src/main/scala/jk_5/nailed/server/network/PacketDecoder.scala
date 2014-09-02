package jk_5.nailed.server.network

import java.io.IOException
import java.util

import com.google.common.collect.BiMap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{ByteToMessageDecoder, CorruptedFrameException}
import net.minecraft.network.{NetworkManager, Packet, PacketBuffer}

/**
 * No description given
 *
 * @author jk-5
 */
class PacketDecoder extends ByteToMessageDecoder {

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]){
    val length = in.readableBytes()

    if(length != 0){
      val packetId = PacketUtils.readVarInt(in)
      val packet = Packet.generatePacket(ctx.channel().attr(NetworkManager.attrKeyReceivable).get().asInstanceOf[BiMap[java.lang.Integer, Class[_ <: Packet]]], packetId)
      if(packet == null){
        throw new CorruptedFrameException("Unknown packet id " + packetId)
      }
      packet.readPacketData(new PacketBuffer(in))
      if(in.readableBytes() > 0){
        throw new IOException("Packet was larger than expected. " + in.readableBytes() + " bytes not read. Packetid: " + packetId + " packetname: " + packet.getClass.getName)
      }
      out.add(packet)
    }
  }
}
