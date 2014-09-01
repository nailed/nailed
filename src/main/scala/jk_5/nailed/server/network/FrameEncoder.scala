package jk_5.nailed.server.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
object FrameEncoder extends MessageToByteEncoder[ByteBuf] {

  override def encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf){
    val size = msg.readableBytes()
    val lengthSize = PacketUtils.varIntSize(size)

    if(lengthSize > 3){
      throw new IllegalArgumentException("Unable to fit " + size + " into 3 bytes")
    }else{
      out.ensureWritable(size + lengthSize)
      PacketUtils.writeVarInt(size, out)
      out.writeBytes(msg, msg.readerIndex(), size)
    }
  }
}
