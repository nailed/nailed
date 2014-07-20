package jk_5.nailed.server

import java.util

import jk_5.eventbus.Event
import jk_5.nailed.api.event._
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.server.command.sender.ConsoleCommandSender
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.BossBar
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.{World, WorldServer}
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedEventFactory {

  object DummyInternalListenerPlugin extends Plugin

  var serverCommandSender: ConsoleCommandSender = _

  private val preTickEvent = new ServerPreTickEvent
  private val postTickEvent = new ServerPostTickEvent

  private val logger = LogManager.getLogger

  def fireEvent[T <: Event](event: T): T = NailedServer.getPluginManager.callEvent(event)

  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, NailedServer)

  def firePreWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPreTickEvent(NailedServer.getWorld(world.provider.dimensionId)))
  }

  def firePostWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPostTickEvent(NailedServer.getWorld(world.provider.dimensionId)))
  }

  def firePreServerTick(server: MinecraftServer) = fireEvent(preTickEvent)
  def firePostServerTick(server: MinecraftServer) = fireEvent(postTickEvent)

  def fireServerStartBeforeConfig(server: DedicatedServer){
    NailedServer.preLoad(server)
    this.serverCommandSender = new ConsoleCommandSender(server)
  }

  def fireServerStarted(server: DedicatedServer){
    NailedServer.load(server)
  }

  def fireStartBeforeWorldLoad(server: DedicatedServer){

  }

  def fireCommand(sender: ICommandSender, input: String): Int = {
    val wrapped = sender match {
      case p: EntityPlayerMP =>
        p.playerNetServerHandler.sendPacket(BossBar.getUpdatePacket(input, input.length))
        NailedServer.getPlayer(p.getGameProfile.getId).orNull
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return -1
    if(NailedServer.getPluginManager.dispatchCommand(wrapped, input, null)) 1 else -1
  }

  def fireTabCompletion(sender: ICommandSender, input: String): util.List[String] = {
    val wrapped = sender match {
      case p: EntityPlayerMP => NailedServer.getPlayer(p.getGameProfile.getId).orNull
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return null
    val ret = mutable.ListBuffer[String]()
    if(NailedServer.getPluginManager.dispatchCommand(wrapped, input, ret)) ret else null
  }

  def fireWorldLoad(world: World){

  }

  def fireWorldUnload(world: World){

  }

  def fireEntityInPortal(entity: Entity){

  }

  def firePlayerJoined(playerEntity: EntityPlayerMP){
    val player = NailedServer.getOrCreatePlayer(playerEntity).asInstanceOf[NailedPlayer]
    player.entity = playerEntity
    player.isOnline = true
    player.world = NailedServer.getWorld(playerEntity.dimension)
    player.netHandler = playerEntity.playerNetServerHandler
    player.world.onPlayerJoined(player)
    player.world.getMap.foreach(_.onPlayerJoined(player))
    val e = this.fireEvent(new PlayerJoinServerEvent(player))
    NailedServer.broadcastMessage(e.joinMessage)
  }

  def firePlayerLeft(playerEntity: EntityPlayerMP){
    val player = NailedServer.getPlayerFromEntity(playerEntity).asInstanceOf[NailedPlayer]
    player.netHandler.sendPacket(BossBar.getDestroyPacket)
    player.world.onPlayerLeft(player)
    player.world.getMap.foreach(_.onPlayerLeft(player))
    player.entity = null
    player.isOnline = false
    player.world = null
    player.netHandler = null
    val e = this.fireEvent(new PlayerLeaveServerEvent(player))
    NailedServer.broadcastMessage(e.leaveMessage)
  }

  def firePlayerChat(playerEntity: EntityPlayerMP, message: String): String = {
    val e = this.fireEvent(new PlayerChatEvent(NailedServer.getPlayerFromEntity(playerEntity), message))
    if(e.isCanceled) null else e.message
  }
}
