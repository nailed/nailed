package jk_5.nailed.server.worlditems

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder, TextComponent}
import jk_5.nailed.api.event.{PlayerJoinMapEvent, PlayerLeaveMapEvent, PlayerRightClickItemEvent, PlayerThrowItemEvent}
import jk_5.nailed.api.material.{ItemStack, Material}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.teleport.Teleporter

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object WorldItemEventHandler {

  //TODO:
  // This is quick and hacky code, just to get a proof-of-concept done.
  // A lot of these methods and events need to be moved to API

  private val tutorialStage = mutable.HashMap[Player, Int]()

  @EventHandler
  def onPlayerJoinMap(event: PlayerJoinMapEvent){
    tutorialStage.remove(event.player)
    if(event.map.mappack.getMetadata.tutorial != null){
      event.player.setInventorySlot(0, getStartTutorialItem)
      tutorialStage.put(event.player, -1)
    }
  }

  @EventHandler
  def onPlayerLeaveMap(event: PlayerLeaveMapEvent){
    var i = 0
    event.player.iterateInventory { s =>
      if(s != null && s.getTag("WorldItemType").isDefined) event.player.setInventorySlot(i, null)
      i += 1
    }
    tutorialStage.remove(event.player)
    event.player.setAllowedToFly(allowed = false)
  }

  @EventHandler
  def onPlayerRightClickItem(event: PlayerRightClickItemEvent){
    val stack = event.stack
    if(stack != null && stack.getTag("WorldItemType").isDefined){
      stack.getTag("WorldItemType").get match {
        case "Tutorial" =>
          event.player.sendMessage(new ComponentBuilder("Starting tutorial").color(ChatColor.GREEN).create())

          removeWorldItemFromPlayer(event.player, "Tutorial")

          event.player.setInventorySlot(0, getNextStageItem)
          event.player.setInventorySlot(8, getEndTutorialItem)

          nextStage(event.player)
          event.player.setAllowedToFly(allowed = true)
          doStageAction(event.player)
          event.player.setAllowedToFly(allowed = true)
        case "Tutorial:NextStage" =>
          nextStage(event.player)
          doStageAction(event.player)
        case "Tutorial:End" =>
          endTutorial(event.player)
        case _ =>
      }
    }
  }

  private def nextStage(player: Player): Int = {
    val current = tutorialStage.getOrElse(player, -1)
    val next = current + 1
    tutorialStage.put(player, next)
    next
  }

  private def endTutorial(player: Player){
    player.sendMessage(new TextComponent(""))
    player.sendMessage(new ComponentBuilder("Finished the tutorial").color(ChatColor.GREEN).create())

    val loc = new Location(player.getWorld.getConfig.spawnPoint)
    loc.setWorld(player.getWorld)
    Teleporter.teleportPlayer(player, new TeleportOptions(loc))

    removeWorldItemFromPlayer(player, "Tutorial:NextStage")
    removeWorldItemFromPlayer(player, "Tutorial:End")

    tutorialStage.remove(player)

    player.setInventorySlot(0, getStartTutorialItem)

    player.setAllowedToFly(allowed = false)
  }

  private def doStageAction(player: Player){
    val nextStage = tutorialStage.getOrElse(player, 0)
    val stages = player.getMap.mappack.getMetadata.tutorial.stages
    if(nextStage >= stages.length){
      endTutorial(player)
    }else{
      val stage = stages(nextStage)
      val loc = new Location(stage.teleport)
      loc.setWorld(player.getWorld)
      Teleporter.teleportPlayer(player, new TeleportOptions(loc))
      player.sendMessage(new TextComponent(""))
      player.sendMessage(new ComponentBuilder("-- " + stage.title).color(ChatColor.DARK_AQUA).create())
      for(line <- stage.messages) player.sendMessage(new ComponentBuilder(line).color(ChatColor.GRAY).create())
    }
  }

  private def removeWorldItemFromPlayer(player: Player, typ: String){
    var i = 0
    player.iterateInventory { s =>
      if(s != null && s.getTag("WorldItemType").isDefined && s.getTag("WorldItemType").get == typ) player.setInventorySlot(i, null)
      i += 1
    }
  }

  def getStartTutorialItem: ItemStack = {
    val is = new ItemStack(Material.EMERALD)
    is.setDisplayName(ChatColor.RESET.toString + ChatColor.GOLD.toString + "Tutorial")
    is.addLore(ChatColor.RESET.toString + ChatColor.GRAY.toString + "Right click to start a tutorial")
    is.setTag("WorldItemType", "Tutorial")
    is
  }

  def getNextStageItem: ItemStack = {
    val is = new ItemStack(Material.EMERALD)
    is.setDisplayName(ChatColor.RESET.toString + ChatColor.GOLD.toString + "Tutorial - Next Stage")
    is.addLore(ChatColor.RESET.toString + ChatColor.GRAY.toString + "Right click to go to the next tutorial stage")
    is.setTag("WorldItemType", "Tutorial:NextStage")
    is
  }

  def getEndTutorialItem: ItemStack = {
    val is = new ItemStack(Material.WOOL, 1, 14)
    is.setDisplayName(ChatColor.RESET.toString + ChatColor.RED.toString + "Stop Tutorial")
    is.addLore(ChatColor.RESET.toString + ChatColor.GRAY.toString + "Right click to stop the tutorial")
    is.setTag("WorldItemType", "Tutorial:End")
    is
  }

  @EventHandler
  def onPlayerThrowItem(event: PlayerThrowItemEvent){
    val stack = event.stack
    if(stack != null && stack.getTag("WorldItemType").isDefined){
      event.setCanceled(true)
    }
  }
}
