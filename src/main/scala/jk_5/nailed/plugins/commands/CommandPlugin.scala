package jk_5.nailed.plugins.commands

import io.netty.util.concurrent.{Future, FutureListener}
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.chat._
import jk_5.nailed.api.command.parametric.annotation.{Optional, Text}
import jk_5.nailed.api.command.sender.{CommandSender, MapCommandSender, WorldCommandSender}
import jk_5.nailed.api.command.{Command, CommandException, Require}
import jk_5.nailed.api.event.RegisterCommandsEvent
import jk_5.nailed.api.map.{GameWinnable, Map, Team}
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.world.{Difficulty, WeatherType}
import jk_5.nailed.api.{GameMode, Platform}
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.mappack.NailedMappackRegistry
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.tweaker.NailedVersion
import jk_5.nailed.server.utils.{InventoryOtherPlayer, NBTUtils}
import net.minecraft.command._
import net.minecraft.command.server._
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow

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
    event.subcommand("team").registerCommandClass(TeamCommand)
    event.subcommand("game").registerCommandClass(GameCommand)
    event.subcommand("map").registerCommandClass(MapCommand)
    event.subcommand("reload").registerCommandClass(ReloadCommand)
    event.subcommand("nailed").registerCommandClass(NailedCommand)

    event.registerCallable(new VanillaCommand(new CommandHelp), "help")
    event.registerCallable(new VanillaCommand(new CommandGive), "give")
    event.registerCallable(new VanillaCommand(new CommandTestForBlock), "testforblock")
    event.registerCallable(new VanillaCommand(new CommandTestFor), "testfor")
    event.registerCallable(new VanillaCommand(new CommandSetBlock), "setblock")
    event.registerCallable(new VanillaCommand(new CommandEffect), "effect")
    event.registerCallable(new VanillaCommand(new CommandTeleport), "tp")
    event.registerCallable(new VanillaCommand(new CommandExecuteAt), "execute")
  }

  object NailedCommand {

    @Command(aliases = Array("version"), desc = "Tells you the nailed version")
    def reloadMappacks(sender: CommandSender){
      val message = new TextComponent("Nailed version " + NailedVersion.full + " implementing api version " + NailedPlatform.getAPIVersion + " for minecraft " + NailedVersion.mcversion)
      message.setColor(ChatColor.GOLD)
      sender.sendMessage(message)
    }
  }

  object ReloadCommand {

    @Command(aliases = Array("mappacks"), desc = "Reload mappacks")
    @Require(Array("admin"))
    def reloadMappacks(){
      NailedMappackRegistry.reload()
    }
  }

  object TeamCommand {

    @Command(aliases = Array("join"), desc = "Join a team")
    @Require(Array("admin"))
    def difficulty(sender: MapCommandSender, player: Player, team: Team){
      val map = sender.getMap
      map.setPlayerTeam(player, team)
      val msg = new ComponentBuilder(s"Player ").color(ChatColor.GREEN).append(player.getName).append(" is now in team ").append(team.name).color(team.color).create()
      map.broadcastChatMessage(msg: _*)
    }
  }

  object GameCommand {

    @Command(aliases = Array("start"), desc = "Starts the game in this map")
    @Require(Array("admin"))
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

    @Command(aliases = Array("stop"), desc = "Stops the game in this map")
    @Require(Array("admin"))
    def stopgame(sender: WorldCommandSender){
      val map = sender.getWorld.getMap
      if(map == null) throw new CommandException("There is no game in this world")
      val manager = map.getGameManager
      if(!manager.isGameRunning) throw new CommandException("No game is running")
      manager.endGame()
      sender.sendMessage(new ComponentBuilder("Ended the game").color(ChatColor.GREEN).create(): _*)
      map.broadcastChatMessage(new ComponentBuilder("The game was stopped by " + sender.getName).color(ChatColor.GOLD).create(): _*)
    }

    @Command(aliases = Array("setwinner"), desc = "Sets the winner")
    @Require(Array("admin"))
    def stopgame(sender: MapCommandSender, winner: GameWinnable){
      sender.getMap.getGameManager.setWinner(winner)
    }
  }

  object MapCommand {

    @Command(aliases = Array("load"), desc = "Loads a new map and registers it to the system")
    @Require(Array("admin"))
    def startgame(platform: Platform, sender: CommandSender, mappack: Mappack){
      val future = platform.getMapLoader.createMapFor(mappack)
      future.addListener(new FutureListener[Map] {
        override def operationComplete(future: Future[Map]){
          //TODO: handle potential future failures
          val builder = new ComponentBuilder("Map ").color(ChatColor.GREEN)
            //.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to go to this map")))
            //.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/goto " + future.get().defaultWorld().getDimensionId)) //TODO: teleport to default world
          builder.append(future.get().mappack.getMetadata.name).append(" was loaded")
          sender.sendMessage(builder.create(): _*)
        }
      })
    }
  }

  @Command(aliases = Array("gamemode", "gm"), desc = "Change your gamemode", usage = "[mode] [target]")
  @Require(Array("admin"))
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
  @Require(Array("admin"))
  def difficulty(sender: WorldCommandSender, difficulty: Difficulty){
    sender.getWorld.setDifficulty(difficulty)
    sender.sendMessage(new ComponentBuilder("Set difficulty to " + difficulty.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("heal"), desc = "Heal yourself or another player")
  @Require(Array("admin"))
  def heal(sender: CommandSender, @Optional target: Player){
    if(target == null){
      sender match {
        case p: Player => p.setHealth(p.getMaxHealth)
        case _ => throw new CommandException("Please specify a player")
      }
    }else{
      target.setHealth(target.getMaxHealth)
    }
  }

  @Command(aliases = Array("kick"), desc = "Kick a player")
  @Require(Array("admin"))
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
  @Require(Array("admin"))
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
  @Require(Array("admin"))
  def weather(sender: WorldCommandSender, weather: WeatherType){
    val world = sender.getWorld
    world.setWeather(weather)
    sender.sendMessage(new ComponentBuilder("Weather changed to " + weather.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("kill"), desc = "Kills a player")
  @Require(Array("admin"))
  def kill(sender: CommandSender, target: Player){
    target.setHealth(0)
    sender.sendMessage(new ComponentBuilder("Killed " + target.getName).color(ChatColor.GREEN).create(): _*)
  }

  @Command(aliases = Array("goto"), desc = "Teleports the player to a world")
  def startgame(platform: Platform, sender: CommandSender, dimension: Int){
    sender match {
      case p: Player => p.teleportTo(platform.getWorld(dimension))
      case _ =>
    }
  }

  @Command(aliases = Array("statemitter"), desc = "Gives you an stat emitter")
  @Require(Array("admin"))
  def startgame(sender: CommandSender, @Optional statName: String){
    val p = sender match {
      case p: Player => p
      case _ => throw new CommandException("You are not a player")
    }
    val is = new ItemStack(Blocks.command_block, 1)
    NBTUtils.getItemNBT(is).setBoolean("IsStatemitter", true)
    NBTUtils.setDisplayName(is, ChatColor.RESET + "Stat Emitter")
    if(statName != null){
      NBTUtils.getItemNBT(is).setString("Content", statName)
      NBTUtils.addLore(is, statName)
    }
    p.asInstanceOf[NailedPlayer].getEntity.inventory.addItemStackToInventory(is)
  }

  @Command(aliases = Array("invsee"), desc = "Look at the inventory of another player")
  @Require(Array("admin"))
  def invsee(sender: CommandSender, player: Player){
    val p = sender match {
      case p: Player => p.asInstanceOf[NailedPlayer]
      case _ => throw new CommandException("You are not a player")
    }
    val entity = p.getEntity
    if(entity.openContainer != entity.inventoryContainer) entity.closeScreen()
    entity.getNextWindowId()

    val chest = new InventoryOtherPlayer(player.asInstanceOf[NailedPlayer].getEntity, entity)
    entity.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(entity.currentWindowId, "minecraft:container", chest.getDisplayName, chest.getSizeInventory))
    entity.openContainer = new ContainerChest(entity.inventory, chest, entity)
    entity.openContainer.windowId = entity.currentWindowId
    entity.openContainer.onCraftGuiOpened(entity)
  }
}
