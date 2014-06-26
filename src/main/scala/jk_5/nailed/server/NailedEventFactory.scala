package jk_5.nailed.server

import jk_5.eventbus.Event
import jk_5.nailed.api.Server
import jk_5.nailed.api.event._
import jk_5.nailed.server.command.sender.ConsoleCommandSender
import jk_5.nailed.server.player.NailedPlayer
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

  var server: Server = _
  private var serverCommandSender: ConsoleCommandSender = _

  private val preTickEvent = new ServerPreTickEvent
  private val postTickEvent = new ServerPreTickEvent

  private def fireEvent(event: Event) = server.getPluginManager.callEvent(event)

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
    //TODO: pool commandsenders
    val wrapped = sender match {
      case p: EntityPlayerMP => new NailedPlayer(p.getGameProfile.getId, p.getGameProfile.getName)//this.server.getPlayer(p.getGameProfile.getId)
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => return -1
    }
    if(server.getPluginManager.dispatchCommand(wrapped, input, null)) 1 else -1
  }

  def fireWorldLoad(world: World){

  }

  def fireWorldUnload(world: World){

  }

  def fireEntityInPortal(entity: Entity){

  }
}
