/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.network

import java.net.{InetSocketAddress, SocketAddress}
import java.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.epoll.{Epoll, EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener, EventLoopGroup}
import jk_5.nailed.server.NailedServer
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.util.ChatComponentText
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedNetworkManager {

  private val disableEpoll = NailedServer.config.getBoolean("netty.disable-epoll")
  private val epollSupported = Epoll.isAvailable
  private val endpoints = mutable.ArrayBuffer[Channel]()
  private[network] val networkManagers = util.Collections.synchronizedList(new util.ArrayList[NetworkManager]())
  private val workers: EventLoopGroup = if(epollSupported && !disableEpoll){
    new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("IO Thread #%d").setDaemon(true).build())
  }else{
    new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("IO Thread #%d").setDaemon(true).build())
  }
  private val logger = LogManager.getLogger

  def addEndpoint(address: SocketAddress){
    val b = new ServerBootstrap()
    if(epollSupported && !disableEpoll){
      b.channel(classOf[EpollServerSocketChannel])
    }else{
      b.channel(classOf[NioServerSocketChannel])
    }
    b.localAddress(address)
    b.childHandler(NettyChannelInitializer)
    b.group(workers)
    logger.info("Opening endpoint " + address.toString)
    b.bind().syncUninterruptibly().addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture){
        endpoints += future.channel()
        logger.info("Endpoint " + address.toString + " started")
      }
    })
  }

  def startEndpoints(){
    for(endpoint <- NailedServer.config.getStringList("endpoints")){
      val parts = endpoint.split(":", 2)
      if(parts.length != 2){
        logger.error(s"Invalid configuration: Server endpoint $endpoint does not specify a port. Ignoring it")
      }else{
        addEndpoint(new InetSocketAddress(parts(0), parts(1).toInt))
      }
    }
  }

  def stopEndpoints(){
    val msg = new ChatComponentText("Server shutting down")
    for(manager <- this.networkManagers){
      manager.scheduleOutboundPacket(new S40PacketDisconnect(msg), new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture){
          manager.closeChannel(msg)
        }
      })
      manager.disableAutoRead()
    }
    for(endpoint <- this.endpoints){
      endpoint.close().syncUninterruptibly()
    }
  }

  def processQueuedPackets(){
    this.networkManagers synchronized {
      val it = networkManagers.iterator
      while(it.hasNext) {
        val manager = it.next()
        if(!manager.isChannelOpen){
          it.remove()
          if(manager.getExitMessage != null){
            manager.getNetHandler.onDisconnect(manager.getExitMessage)
          }else if(manager.getNetHandler != null){
            manager.getNetHandler.onDisconnect(new ChatComponentText("Disconnected"))
          }
        }else{
          try{
            manager.processReceivedPackets()
          }catch{
            case e: Exception =>
              logger.warn("Error while handling packet for client " + manager.getSocketAddress, e)
              val msg = new ChatComponentText("Internal server error")
              manager.scheduleOutboundPacket(new S40PacketDisconnect(msg), new ChannelFutureListener {
                override def operationComplete(future: ChannelFuture){
                  manager.closeChannel(msg)
                }
              })
              manager.disableAutoRead()
          }
        }
      }
    }
  }
}
