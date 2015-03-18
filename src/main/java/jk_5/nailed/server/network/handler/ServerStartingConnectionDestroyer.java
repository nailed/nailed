package jk_5.nailed.server.network.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jk_5.nailed.server.network.NettyChannelInitializer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.util.ChatComponentText;

@ChannelHandler.Sharable
public class ServerStartingConnectionDestroyer extends SimpleChannelInboundHandler<C00Handshake> {

    private ServerStartingConnectionDestroyer() {
        super(false);
    }

    public static final ServerStartingConnectionDestroyer INSTANCE = new ServerStartingConnectionDestroyer();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, C00Handshake packet) throws Exception {
        if(!NettyChannelInitializer.serverStarting){
            ctx.fireChannelRead(packet);
            ctx.pipeline().remove(this);
        }else{
            ctx.writeAndFlush(new S00PacketDisconnect(new ChatComponentText("Server is still starting! Please wait before connecting!"))).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
