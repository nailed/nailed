package jk_5.nailed.server.network.handler

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import jk_5.nailed.server.network.NettyChannelInitializer
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.login.server.S00PacketDisconnect
import net.minecraft.util.ChatComponentText

/**
 * No description given
 *
 * @author jk-5
 */
@Sharable
object ServerStartingConnectionDestroyer extends SimpleChannelInboundHandler[C00Handshake](false){

  override def channelRead0(ctx: ChannelHandlerContext, msg: C00Handshake){
    if(!NettyChannelInitializer.serverStarting){
      ctx.fireChannelRead(msg)
      ctx.pipeline().remove(this)
    }else{
      ctx.writeAndFlush(new S00PacketDisconnect(new ChatComponentText("Server is still starting! Please wait before connecting!"))).addListener(ChannelFutureListener.CLOSE)
    }
  }
}
