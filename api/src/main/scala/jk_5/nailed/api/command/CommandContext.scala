package jk_5.nailed.api.command

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, ComponentBuilder}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
final class CommandContext(private val _sender: CommandSender) extends CommandSender {

  private var analogOutput = 1

  def setAnalogOutput(output: Int){
    this.analogOutput = if(output < 0) 0 else if(output > 15) 15 else output
  }

  def getAnalogOutput = this.analogOutput

  @inline def wrongUsage(usage: String): CommandUsageException = exception(new CommandUsageException(usage))
  @inline def requireLocation(): Location = if(!_sender.isInstanceOf[LocationCommandSender]) throw exception(new CommandException("You don\'t have a location")) else _sender.asInstanceOf[LocationCommandSender].getLocation
  @inline def requirePlayer(): Player = if(!_sender.isInstanceOf[Player]) throw exception(new NotAPlayerException) else _sender.asInstanceOf[Player]
  @inline def requireWorld(): World = if(!_sender.isInstanceOf[WorldCommandSender]) throw exception(new NoWorldException) else _sender.asInstanceOf[WorldCommandSender].getWorld
  @inline def error(message: String): CommandException = exception(new CommandException(message))
  @inline def success(message: String): Unit = _sender.sendMessage(new ComponentBuilder(message).color(ChatColor.GREEN).create())
  @inline def exception[T <: CommandException](e: T): T = {setAnalogOutput(0); e}

  def sender = _sender
  override def getName: String = _sender.getName
  override def sendMessage(message: BaseComponent): Unit = _sender.sendMessage(message)
  override def sendMessage(messages: BaseComponent*): Unit = _sender.sendMessage(messages: _*)
  override def sendMessage(messages: Array[BaseComponent]): Unit = _sender.sendMessage(messages)
  override def hasPermission(permission: String): Boolean = _sender.hasPermission(permission)
  override def getDescriptionComponent: BaseComponent = _sender.getDescriptionComponent
}
