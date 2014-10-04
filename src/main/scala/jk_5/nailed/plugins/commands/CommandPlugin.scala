package jk_5.nailed.plugins.commands

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder, TextComponent}
import jk_5.nailed.api.command.parametric.annotation.{Optional, Text}
import jk_5.nailed.api.command.sender.{CommandSender, WorldCommandSender}
import jk_5.nailed.api.command.{Command, CommandException}
import jk_5.nailed.api.event.RegisterCommandsEvent
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.world.{Difficulty, WeatherType}
import jk_5.nailed.api.{GameMode, Platform}
import net.minecraft.command.CommandHelp

/**
 * No description given
 *
 * @author jk-5
 */
@Plugin(id = "Nailed|Commands", name = "Nailed Commands", version = "1.0.0")
class CommandPlugin {

  @EventHandler
  def registerCommands(event: RegisterCommandsEvent){
    event.registerCommandClass(this)

    event.registerCallable(new VanillaCommand(new CommandHelp), "help")
  }

  @Command(aliases = Array("gamemode", "gm"), desc = "Change your gamemode", usage = "[mode] [target]")
  def gamemode(sender: CommandSender, @Optional mode: GameMode, @Optional target: Player){
    if(mode == null){
      sender match {
        case p: Player =>
          val current = p.getGameMode
          if(current == GameMode.CREATIVE){
            p.setGameMode(GameMode.SURVIVAL)
          }else{
            p.setGameMode(GameMode.CREATIVE)
          }
        case _ => throw new CommandException("Please specify a gamemode and player")
      }
    }else{
      if(target == null){
        sender match {
          case p: Player => p.setGameMode(mode)
          case _ => throw new CommandException("Please specify a player")
        }
      }else{
        target.setGameMode(mode)
        sender.sendMessage(new ComponentBuilder("Set gamemode to " + mode.getName).color(ChatColor.GREEN).create(): _*)
      }
    }
  }

  @Command(aliases = Array("difficulty"), desc = "Change the world difficulty")
  def difficulty(sender: WorldCommandSender, difficulty: Difficulty){
    sender.getWorld.setDifficulty(difficulty)
    sender.sendMessage(new ComponentBuilder("Set difficulty to " + difficulty.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("heal"), desc = "Heal yourself or another player")
  def heal(sender: CommandSender, @Optional target: Player){
    if(target == null){
      sender match {
        case p: Player => p.setHealth(p.getMaxHealth)
        case _ => throw new CommandException("Please specify and player")
      }
    }else{
      target.setHealth(target.getMaxHealth)
    }
  }

  @Command(aliases = Array("kick"), desc = "Kick a player")
  def kick(platform: Platform, sender: CommandSender, target: Player, @Optional @Text r: String){
    val reason = if(r == null) "No reason given" else r
    target.kick("Kicked by " + sender.getName + ". Reason: " + reason)

    val b = new TextComponent("")
    b.setColor(ChatColor.RED)
    b.addExtra("Player ")
    b.addExtra(target.getDescriptionComponent)
    b.addExtra(" was kicked by ")
    b.addExtra(sender.getDescriptionComponent)
    platform.broadcastMessage(b)
    platform.broadcastMessage(new ComponentBuilder("Reason: " + reason).color(ChatColor.RED).create(): _*)

    val m = new TextComponent("Successfully kicked player ")
    m.addExtra(target.getDescriptionComponent)
    m.setColor(ChatColor.GREEN)
    sender.sendMessage(m)
  }

  @Command(aliases = Array("toggledownfall"), desc = "Toggles rain")
  def toggledownfall(sender: WorldCommandSender){
    val world = sender.getWorld
    if(world.getWeather.isRaining){
      world.setWeather(WeatherType.DRY)
      sender.sendMessage(new ComponentBuilder("Weather changed to dry").color(ChatColor.GREEN).create(): _*)
    }else{
      world.setWeather(WeatherType.RAIN)
      sender.sendMessage(new ComponentBuilder("Weather changed to raining").color(ChatColor.GREEN).create(): _*)
    }
  }

  @Command(aliases = Array("weather"), desc = "Changes the weather")
  def weather(sender: WorldCommandSender, weather: WeatherType){
    val world = sender.getWorld
    world.setWeather(weather)
    sender.sendMessage(new ComponentBuilder("Weather changed to " + weather.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("kill"), desc = "Kills a player")
  def kill(sender: CommandSender, target: Player){
    target.setHealth(0)
    sender.sendMessage(new ComponentBuilder("Killed " + target.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("startgame"), desc = "Starts the game in this map")
  def startgame(sender: WorldCommandSender){
    val map = sender.getWorld.getMap
    if(map == null) throw new CommandException("There is no game in this world")
    val manager = map.getGameManager
    if(manager.isGameRunning) throw new CommandException("A game is already running")
    if(manager.startGame()){
      sender.sendMessage(new ComponentBuilder("Started the game").color(ChatColor.GREEN).create(): _*)
    }else{
      if(manager.hasCustomGameType){
        throw new CommandException("Could not start the game. An error has occurred in the GameType")
      }else{
        throw new CommandException("Could not start the game. No game.js was found")
      }
    }
  }
}
