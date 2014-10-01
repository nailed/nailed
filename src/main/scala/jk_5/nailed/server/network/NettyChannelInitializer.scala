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

import io.netty.channel.{Channel, ChannelException, ChannelInitializer, ChannelOption}
import io.netty.handler.timeout.ReadTimeoutHandler
import jk_5.nailed.server.network.handler.ServerStartingConnectionDestroyer
import net.minecraft.network.{EnumPacketDirection, NetworkManager}
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.NetHandlerHandshakeTCP
import net.minecraft.util._

/**
 * No description given
 *
 * @author jk-5
 */
object NettyChannelInitializer extends ChannelInitializer[Channel] {

  var serverStarting = true

  override def initChannel(ch: Channel){
    try{
      ch.config().setOption(ChannelOption.IP_TOS, 24: java.lang.Integer)
    }catch{
      case e: ChannelException =>
    }

    try{
      ch.config().setOption(ChannelOption.TCP_NODELAY, false: java.lang.Boolean)
    }catch{
      case e: ChannelException =>
    }

    val pipe = ch.pipeline()
    pipe.addLast("timeout", new ReadTimeoutHandler(30))
    //pipe.addLast("legacy_query", new PingResponseHandler) //TODO
    //pipe.addLast("splitter", new FrameDecoder)
    //pipe.addLast("decoder", new PacketDecoder)
    //pipe.addLast("prepender", FrameEncoder)
    //pipe.addLast("encoder", PacketEncoder)
    pipe.addLast("splitter", new MessageDeserializer2)
    pipe.addLast("decoder", new MessageDeserializer(EnumPacketDirection.SERVERBOUND))
    pipe.addLast("prepender", new MessageSerializer2)
    pipe.addLast("encoder", new MessageSerializer(EnumPacketDirection.CLIENTBOUND))

    if(serverStarting) pipe.addLast(ServerStartingConnectionDestroyer)

    val manager = new NetworkManager(EnumPacketDirection.SERVERBOUND)
    NailedNetworkManager.networkManagers.add(manager)
    pipe.addLast("packet_handler", manager)
    manager.setNetHandler(new NetHandlerHandshakeTCP(MinecraftServer.getServer, manager))
  }
}
