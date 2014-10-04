package jk_5.nailed.plugins.commands

import jk_5.nailed.api
import jk_5.nailed.api.command.context.CommandLocals
import jk_5.nailed.api.command.{CommandCallable, SettableDescription}
import net.minecraft.command.{CommandBase, CommandException, ICommandSender}
import net.minecraft.util.BlockPos

/**
 * No description given
 *
 * @author jk-5
 */
class VanillaCommand(val wrapped: CommandBase) extends CommandCallable {

  override def call(arguments: String, locals: CommandLocals, parentCommands: Array[String]) = {
    val split = arguments.split(" ", -1)
    try{
      wrapped.processCommand(locals.get(classOf[ICommandSender]), split)
    }catch{
      case e: CommandException => throw new api.command.CommandException(e.getMessage.format(e.getErrorOjbects: _*), e.getCause)
    }
    true
  }

  override def testPermission(locals: CommandLocals): Boolean = {
    wrapped.canCommandSenderUseCommand(locals.get(classOf[ICommandSender]))
  }

  override val getDescription = new SettableDescription

  override def getSuggestions(arguments: String, locals: CommandLocals) = {
    wrapped.addTabCompletionOptions(locals.get(classOf[ICommandSender]), arguments.split(" ", -1), BlockPos.ORIGIN).asInstanceOf[java.util.List[String]]
  }
}
