package jk_5.nailed.server.network;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import jk_5.nailed.server.network.handler.ServerStartingConnectionDestroyer;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;

@ChannelHandler.Sharable
public class NettyChannelInitializer extends ChannelInitializer<Channel> {

    public static final NettyChannelInitializer INSTANCE = new NettyChannelInitializer();
    public static boolean serverStarting = false;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        try{
            ch.config().setOption(ChannelOption.IP_TOS, 24);
        }catch(ChannelException e){
        }

        try{
            ch.config().setOption(ChannelOption.TCP_NODELAY, false);
        }catch(ChannelException e){
        }

        ChannelPipeline pipe = ch.pipeline();
        pipe.addLast("timeout", new ReadTimeoutHandler(30));
        //pipe.addLast("legacy_query", new PingResponseHandler) //TODO
        pipe.addLast("splitter", new MessageDeserializer2());
        pipe.addLast("decoder", new MessageDeserializer(EnumPacketDirection.SERVERBOUND));
        pipe.addLast("prepender", new MessageSerializer2());
        pipe.addLast("encoder", new MessageSerializer(EnumPacketDirection.CLIENTBOUND));

        if(serverStarting) pipe.addLast(ServerStartingConnectionDestroyer.INSTANCE);

        NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
        NailedNetworkManager.networkManagers.add(manager);
        pipe.addLast("packet_handler", manager);
        manager.setNetHandler(new NetHandlerHandshakeTCP(MinecraftServer.getServer(), manager));
    }
}
