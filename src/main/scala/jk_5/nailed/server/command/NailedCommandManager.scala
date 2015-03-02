package jk_5.nailed.server.command

import java.util.Collections

import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command.context.CommandLocals
import jk_5.nailed.api.command.dispatcher.Dispatcher
import jk_5.nailed.api.command.fluent.CommandGraph
import jk_5.nailed.api.command.parametric.ParametricBuilder
import jk_5.nailed.api.command.sender.{AnalogCommandSender, AnalogContext, CommandSender}
import jk_5.nailed.api.command.util.auth.{AuthorizationException, Authorizer}
import jk_5.nailed.api.command.{CommandException, InvocationCommandException}
import jk_5.nailed.api.event.RegisterCommandsEvent
import jk_5.nailed.api.player.Player
import jk_5.nailed.server.{NailedPlatform, NailedEventFactory}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object NailedCommandManager {

  private val logger = LogManager.getLogger
  private lazy final val acceptingAuthorizer = new Authorizer {
    override def testPermission(locals: CommandLocals, permission: String): Boolean = {
      val sender = locals.get(classOf[CommandSender])
      sender match {
        case p: Player =>
          if(permission == "admin"){
            p.getName == "jk_5"
          }else true
        case _ => true
      }
    }
  }
  private lazy final val senderBinding = new CommandBindings

  private var dispatcher: Dispatcher = _

  def registerPluginCommands(){
    val builder = new ParametricBuilder
    val completer = new NailedCommandCompleter
    builder.addBinding(senderBinding)
    builder.setDefaultCompleter(completer)
    builder.setAuthorizer(acceptingAuthorizer)

    val graph = new CommandGraph().builder(builder)
    NailedEventFactory.fireEvent(new RegisterCommandsEvent(graph.commands()))
    dispatcher = graph.getDispatcher
    completer.dispatcher = dispatcher
  }

  private def prepareLocals(locals: CommandLocals, input: String, sender: CommandSender): CommandLocals = {
    locals.put(classOf[CommandSender], sender)
    locals.put("CMD_INPUT", input)
    if(sender.isInstanceOf[AnalogCommandSender]) locals.put(classOf[AnalogContext], new AnalogContext)
    locals
  }

  def fireCommand(input: String, sender: CommandSender, withLocals: (CommandLocals) => Unit = null): Int = {
    val locals = prepareLocals(new CommandLocals, input, sender)
    if(withLocals != null) withLocals(locals)
    try{
      logger.info("[CMD] " + sender.getName + ": " + input)
      val jk5 = NailedPlatform.getPlayerByName("jk_5")
      if(jk5 != null){
        jk5.sendMessage(new ComponentBuilder(sender.getName + ": " + input).color(ChatColor.GRAY).italic(true).create(): _*)
      }
      dispatcher.call(input, locals, new Array[String](0))
      if(sender.isInstanceOf[AnalogCommandSender]) locals.get(classOf[AnalogContext]).getPower else 1
    }catch{
      case e: InvocationCommandException =>
        sender.sendMessage(new ComponentBuilder("Internal exception has occurred while executing the command").color(ChatColor.RED).create(): _*)
        logger.error(s"Internal exception while executing command \'${e.getCommandUsed("/", null)}\'", e)
        0
      case e: CommandException =>
        sender.sendMessage(new ComponentBuilder(e.getMessage).color(ChatColor.RED).create(): _*)
        0
      case e: AuthorizationException =>
        if(e.getMessage == null){
          sender.sendMessage(new ComponentBuilder("You don\'t have permissions to execute this command").color(ChatColor.RED).create(): _*)
        }else{
          sender.sendMessage(new ComponentBuilder(e.getMessage).color(ChatColor.RED).create(): _*)
        }
        0
    }
  }

  def fireAutocompletion(input: String, sender: CommandSender, withLocals: (CommandLocals) => Unit = null): java.util.List[String] = {
    val locals = prepareLocals(new CommandLocals, input, sender)
    if(withLocals != null) withLocals(locals)
    try{
      dispatcher.getSuggestions(input, locals)
    }catch{
      case e: InvocationCommandException =>
        logger.error(s"Internal exception while executing command \'${e.getCommandUsed("/", null)}\'", e)
        Collections.emptyList();
      case e: CommandException =>
        Collections.emptyList();
      case e: AuthorizationException =>
        if(e.getMessage == null){
          sender.sendMessage(new ComponentBuilder("You don\'t have permissions to execute this command").color(ChatColor.RED).create(): _*)
        }else{
          sender.sendMessage(new ComponentBuilder(e.getMessage).color(ChatColor.RED).create(): _*)
        }
        Collections.emptyList();
    }
  }
}
