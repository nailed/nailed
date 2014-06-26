package jk_5.nailed.server

import jk_5.eventbus.Event
import jk_5.nailed.api.event._
import jk_5.nailed.internalplugin.NailedInternalPlugin
import jk_5.nailed.server.command.sender.ConsoleCommandSender
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.{World, WorldServer}

/**
 * No description given
 *
 * @author jk-5
 */
object NailedEventFactory {

  private var serverCommandSender: ConsoleCommandSender = _

  private val preTickEvent = new ServerPreTickEvent
  private val postTickEvent = new ServerPreTickEvent

  private def fireEvent[T <: Event](event: T): T = NailedServer.getPluginManager.callEvent(event)

  NailedServer.getPluginManager.registerListener(NailedInternalPlugin, NailedServer)

  def firePreWorldTick(server: MinecraftServer, world: WorldServer) = fireEvent(new WorldPreTickEvent(world))
  def firePostWorldTick(server: MinecraftServer, world: WorldServer) = fireEvent(new WorldPostTickEvent(world))
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
      case p: EntityPlayerMP => NailedServer.getPlayer(p.getGameProfile.getId).orNull
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return -1
    if(NailedServer.getPluginManager.dispatchCommand(wrapped, input, null)) 1 else -1
  }

  def fireWorldLoad(world: World){

  }

  def fireWorldUnload(world: World){

  }

  def fireEntityInPortal(entity: Entity){

  }

  def firePlayerJoined(playerEntity: EntityPlayerMP){
    val player = NailedServer.getOrCreatePlayer(playerEntity)
    val e = this.fireEvent(new PlayerJoinServerEvent(player))
    NailedServer.broadcastMessage(e.joinMessage)
  }

  def firePlayerLeft(playerEntity: EntityPlayerMP){
    val player = NailedServer.getPlayerFromEntity(playerEntity)
    val e = this.fireEvent(new PlayerLeaveServerEvent(player))
    NailedServer.broadcastMessage(e.leaveMessage)
  }
}
