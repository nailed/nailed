package jk_5.nailed.server.network

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.CorruptedFrameException

/**
 * No description given
 *
 * @author jk-5
 */
object PacketUtils {

  def varIntSize(int: Int): Int =
    if((int & -128) == 0) 1
    else if((int & -16384) == 0) 2
    else if((int & -2097152) == 0) 3
    else if((int & -268435456) == 0) 4
    else 5

  def writeVarInt(int: Int, buffer: ByteBuf){
    var i = int
    while((i & -128) != 0){
      buffer.writeByte(i & 127 | 128)
      i >>>= 7
    }
    buffer.writeByte(i)
  }

  def readVarInt(buffer: ByteBuf): Int = {
    var ret = 0
    var length = 0
    var read: Byte = 0

    do{
      read = buffer.readByte()
      ret |= (read & 127) << length * 7
      length += 1
      if(length > 5){
        throw new CorruptedFrameException("VarInt too big")
      }
    }while((read & 128) == 128)

    ret
  }
}
