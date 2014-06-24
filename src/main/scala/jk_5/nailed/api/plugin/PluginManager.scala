package jk_5.nailed.api.plugin

import java.util
import java.util.regex.Pattern

import com.google.common.collect.{ArrayListMultimap, Multimap}
import jk_5.eventbus.EventBus
import jk_5.nailed.api.NailedServer
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder, TabExecutor}
import jk_5.nailed.api.command.CommandSender
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class PluginManager(private val server: NailedServer) {

  private val eventBus = new EventBus
  private val plugins = mutable.HashMap[String, Plugin]()
  private val commandMap = mutable.HashMap[String, Command]()
  private val commandsByPlugin: Multimap[Plugin, Command] = ArrayListMultimap.create()
  private val argsSplit = Pattern.compile(" ")
  private val logger = LogManager.getLogger

  /**
   * Register a command so that it may be executed.
   *
   * @param plugin the plugin owning this command
   * @param command the command to register
   */
  def registerCommand(plugin: Plugin, command: Command){
    commandMap.put(command.getName.toLowerCase, command)
    command.getAliases.foreach(a => commandMap.put(a.toLowerCase, command))
    commandsByPlugin.put(plugin, command)
  }

  /**
   * Unregister a command so it will no longer be executed.
   *
   * @param command the command to unregister
   */
  def unregisterCommand(command: Command){
    val removals = mutable.ArrayBuffer[String]()
    commandMap.foreach(e => if(e._2 == command) removals += e._1)
    removals.foreach(commandMap.remove)
    commandsByPlugin.values().remove(command)
  }

  /**
   * Unregister all commands owned by a {@link Plugin}
   *
   * @param plugin the plugin to unregister the commands of
   */
  def unregisterCommands(plugin: Plugin){
    val it = commandsByPlugin.get(plugin).iterator()
    while(it.hasNext){
      val c = it.next()
      val removals = mutable.ArrayBuffer[String]()
      commandMap.foreach(e => if(e._2 == c) removals += e._1)
      removals.foreach(commandMap.remove)
      it.remove()
    }
  }

  def dispatchCommand(sender: CommandSender, line: String): Boolean = dispatchCommand(sender, line, null)
  def dispatchCommand(sender: CommandSender, line: String, tabResults: mutable.ListBuffer[String]): Boolean = {
    val split = argsSplit.split(line)
    if(split.length == 0) return false
    val commandName = split(0).toLowerCase
    val command = commandMap.get(commandName)
    if(command.isEmpty) return false
    val args = util.Arrays.copyOfRange(split, 1, split.length)
    try{
      if(tabResults == null){
        command.get.execute(sender, args)
      }else command match {
        case t: TabExecutor =>
          tabResults ++= t.onTabComplete(sender, args)
        case _ =>
      }
    }catch{
      case e: Exception =>
        sender.sendMessage(new ComponentBuilder("An internal error occurred whilst executing this command, please check the console log for details.").color(ChatColor.red).create())
        logger.warn("Error in dispatching command", e)
    }
    true
  }

  /**
   * Returns the {@link Plugin} objects corresponding to all loaded plugins.
   *
   * @return the set of loaded plugins
   */
  def getPlugins = this.plugins.values

  /**
   * Returns a loaded plugin identified by the specified name.
   *
   * @param name of the plugin to retrieve
   * @return the retrieved plugin or null if not loaded
   */
  def getPlugin(name: String) = this.plugins.get(name).orNull
}
