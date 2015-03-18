package jk_5.nailed.server.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jk_5.nailed.server.NailedPlatform;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NailedNetworkManager {

    private static final boolean disableEpoll = NailedPlatform.config().getBoolean("network.disable-epoll");
    private static final boolean epollSupported = Epoll.isAvailable();
    private static final List<Channel> endpoints = new ArrayList<>();
    static final List<NetworkManager> networkManagers = Collections.synchronizedList(new ArrayList<>());
    private static final EventLoopGroup workers;
    private static final Logger logger = LogManager.getLogger();

    static {
        if(epollSupported && !disableEpoll){
            workers = new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("IO Thread #%d").setDaemon(true).build());
        }else{
            workers = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("IO Thread #%d").setDaemon(true).build());
        }
    }

    public static void addEndpoint(SocketAddress address){
        ServerBootstrap bootstrap = new ServerBootstrap();
        if(epollSupported && !disableEpoll){
            bootstrap.channel(EpollServerSocketChannel.class);
        }else{
            bootstrap.channel(NioServerSocketChannel.class);
        }
        bootstrap.localAddress(address);
        bootstrap.childHandler(NettyChannelInitializer.INSTANCE);
        bootstrap.group(workers);
        logger.info("Opening endpoint " + address.toString());
        bootstrap.bind().syncUninterruptibly().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future){
                endpoints.add(future.channel());
                logger.info("Endpoint " + address.toString() + " started");
            }
        });
    }

    public static void startEndpoints(){
        logger.info("Starting server listeners");
        if(epollSupported && disableEpoll){
            logger.info("Epoll transport is supported, but disabled in the config. Falling back to NIO transport");
        }else if(epollSupported){
            logger.info("Epoll transport is supported and will be used");
        }else{
            logger.info("Epoll transport is not supported. Falling back to NIO transport");
        }
        for(String endpoint : NailedPlatform.config().getStringList("network.endpoints")){
            String[] parts = endpoint.split(":", 2);
            if(parts.length != 2){
                logger.error("Invalid configuration: Server endpoint " + endpoint + " does not specify a port. Ignoring it");
            }else{
                addEndpoint(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
            }
        }
    }

    public static void stopEndpoints(){
        ChatComponentText msg = new ChatComponentText("Server shutting down");
        for(NetworkManager manager : networkManagers){
            manager.sendPacket(new S40PacketDisconnect(msg), future -> manager.closeChannel(msg));
            manager.disableAutoRead();
        }
        endpoints.forEach(endpoint -> endpoint.close().syncUninterruptibly());
    }

    public static void processQueuedPackets(){
        synchronized (networkManagers){
            Iterator<NetworkManager> it = networkManagers.iterator();
            while(it.hasNext()){
                NetworkManager manager = it.next();
                if(!manager.isChannelOpen()){
                    it.remove();
                    if(manager.getExitMessage() != null){
                        manager.getNetHandler().onDisconnect(manager.getExitMessage());
                    }else if(manager.getNetHandler() != null){
                        manager.getNetHandler().onDisconnect(new ChatComponentText("Disconnected"));
                    }
                }else{
                    try{
                        manager.processReceivedPackets();
                    }catch(Exception e){
                        logger.warn("Error while handling packet for client " + manager.getRemoteAddress(), e);
                        ChatComponentText msg = new ChatComponentText("Internal server error");
                        manager.sendPacket(new S40PacketDisconnect(msg), future -> manager.closeChannel(msg));
                        manager.disableAutoRead();
                    }
                }
            }
        }
    }
}
