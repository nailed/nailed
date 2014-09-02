package jk_5.nailed.server.network

import java.util

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{ByteToMessageDecoder, CorruptedFrameException}

/**
 * No description given
 *
 * @author jk-5
 */
class FrameDecoder extends ByteToMessageDecoder {

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]){
    in.markReaderIndex()
    val lengthBytes = new Array[Byte](3)

    for(i <- 0 until lengthBytes.length){
      if(!in.isReadable){
        in.resetReaderIndex()
        return //Stop decoding without reading and writing. The cumulation will be saved and decoded again later
      }
      lengthBytes(i) = in.readByte()

      if(lengthBytes(i) >= 0){
        val buffer = Unpooled.wrappedBuffer(lengthBytes)
        try{
          val length = PacketUtils.readVarInt(buffer)
          if(in.readableBytes() >= length){
            out.add(in.readBytes(length))
            return
          }
          in.resetReaderIndex()
        }finally{
          buffer.release()
        }
        return
      }
    }
    throw new CorruptedFrameException("Length wider than 21-bit")
  }
}
