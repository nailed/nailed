package jk_5.nailed.server.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{EncoderException, MessageToByteEncoder}
import net.minecraft.network._
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
object PacketEncoder extends MessageToByteEncoder[Packet] {

  val logger = LogManager.getLogger

  override def encode(ctx: ChannelHandlerContext, msg: Packet, out: ByteBuf){
    val connectionState = ctx.channel().attr(NetworkManager.attrKeyConnectionState).get().asInstanceOf[EnumConnectionState]
    val packetId = connectionState.getPacketId(EnumPacketDirection.SERVERBOUND, msg)

    if(packetId == null){
      throw new EncoderException("Can't serialize unregistered packet")
    }

    PacketUtils.writeVarInt(packetId, out)
    try{
      msg.writePacketData(new PacketBuffer(out))
    }catch{
      case e: Throwable => logger.error("Exception while writing packet " + packetId + " (" + msg.getClass.getSimpleName + ")", e)
    }
  }
}
