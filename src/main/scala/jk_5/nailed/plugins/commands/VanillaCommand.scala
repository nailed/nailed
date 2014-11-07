package jk_5.nailed.plugins.commands

import jk_5.nailed.api.command.context.CommandLocals
import jk_5.nailed.api.command.sender.AnalogContext
import jk_5.nailed.api.command.{CommandCallable, SettableDescription}
import net.minecraft.command._
import net.minecraft.entity.Entity
import net.minecraft.util.{BlockPos, ChatComponentTranslation, EnumChatFormatting}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class VanillaCommand(val wrapped: CommandBase) extends CommandCallable {

  val logger = LogManager.getLogger

  override def call(arguments: String, locals: CommandLocals, parentCommands: Array[String]) = {
    val split = arguments.split(" ", -1)

    val analog = Option(locals.get(classOf[AnalogContext]))
    val usernameIndex = getUsernameIndex(split)
    val sender = locals.get(classOf[ICommandSender])
    val input = locals.get("CMD_INPUT").asInstanceOf[String]
    var value = 0

    if(usernameIndex > -1){ //Has username index
      val selector = split(usernameIndex)
      val selected = PlayerSelector.matchEntities(sender, selector, classOf[Entity])
      sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, selected.size())
      val it = selected.iterator()
      while(it.hasNext){
        val entity = it.next().asInstanceOf[Entity]
        split(usernameIndex) = entity.getUniqueID.toString
        if(this.execute(sender, split, input)) value += 1
      }
    }else{ //No username index
      sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1)
      if(execute(sender, split, input)) value += 1
    }
    analog.foreach(_.setPower(value))
    sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, value)
    true
  }

  private def execute(sender: ICommandSender, args: Array[String], input: String): Boolean = {
    try{
      wrapped.processCommand(sender, args)
      return true
    }catch{
      case e: WrongUsageException =>
        val comp = new ChatComponentTranslation("commands.generic.usage", new ChatComponentTranslation(e.getMessage), e.getErrorObjects)
        comp.getChatStyle.setColor(EnumChatFormatting.RED)
        sender.addChatMessage(comp)
      case e: CommandException =>
        val comp = new ChatComponentTranslation(e.getMessage, e.getErrorObjects)
        comp.getChatStyle.setColor(EnumChatFormatting.RED)
        sender.addChatMessage(comp)
      case t: Throwable =>
        val comp = new ChatComponentTranslation("commands.generic.exception")
        comp.getChatStyle.setColor(EnumChatFormatting.RED)
        sender.addChatMessage(comp)
        logger.error("Couldn\'t process command: \'" + input + "\'", t)
    }
    false
  }

  override def testPermission(locals: CommandLocals): Boolean = {
    wrapped.canCommandSenderUseCommand(locals.get(classOf[ICommandSender]))
  }

  override val getDescription = new SettableDescription

  override def getSuggestions(arguments: String, locals: CommandLocals) = {
    wrapped.addTabCompletionOptions(locals.get(classOf[ICommandSender]), arguments.split(" ", -1), BlockPos.ORIGIN).asInstanceOf[java.util.List[String]]
  }

  private def getUsernameIndex(args: Array[String]): Int = {
    var i = 0
    args.foreach { a =>
      if(wrapped.isUsernameIndex(args, i) && PlayerSelector.matchesMultiplePlayers(a)){
        return i
      }
      i += 1
    }
    -1
  }
}
