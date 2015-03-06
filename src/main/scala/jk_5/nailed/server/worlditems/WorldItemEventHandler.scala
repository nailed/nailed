/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.worlditems

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder, TextComponent}
import jk_5.nailed.api.event.player.{PlayerJoinMapEvent, PlayerLeaveMapEvent, PlayerRightClickItemEvent, PlayerThrowItemEvent}
import jk_5.nailed.api.item.{ItemStack, Material}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.{Location, TeleportOptions}
import jk_5.nailed.server.teleport.Teleporter

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object WorldItemEventHandler {

  //TODO:
  // Unmount before teleporting

  private val tutorialStage = mutable.HashMap[Player, Int]()

  @EventHandler
  def onPlayerJoinMap(event: PlayerJoinMapEvent){
    tutorialStage.remove(event.getPlayer)
    if(event.getMap.mappack.getMetadata.tutorial != null){
      //event.getPlayer.setInventorySlot(0, getStartTutorialItem) //TODO
      tutorialStage.put(event.getPlayer, -1)
    }
  }

  @EventHandler
  def onPlayerLeaveMap(event: PlayerLeaveMapEvent){
    var i = 0
    /*event.getPlayer.iterateInventory { s =>
      if(s != null && s.getTag("WorldItemType").isDefined) event.getPlayer.setInventorySlot(i, null)
      i += 1
    }*/ //TODO
    tutorialStage.remove(event.getPlayer)
    event.getPlayer.setAllowedToFly(false) //TODO don't set this to false if the player is creative or was allowed to fly
  }

  @EventHandler
  def onPlayerRightClickItem(event: PlayerRightClickItemEvent){
    val stack = event.getStack
    if(stack != null && stack.getTag("WorldItemType") != null){
      stack.getTag("WorldItemType") match {
        case "Tutorial" =>
          event.getPlayer.sendMessage(new ComponentBuilder("Starting tutorial").color(ChatColor.GREEN).create(): _*)

          removeWorldItemFromPlayer(event.getPlayer, "Tutorial")

          //event.getPlayer.setInventorySlot(0, getNextStageItem) //TODO
          //event.getPlayer.setInventorySlot(8, getEndTutorialItem) //TODO

          nextStage(event.getPlayer)
          event.getPlayer.setAllowedToFly(true)
          doStageAction(event.getPlayer)
          event.getPlayer.setAllowedToFly(true)
        case "Tutorial:NextStage" =>
          nextStage(event.getPlayer)
          doStageAction(event.getPlayer)
        case "Tutorial:End" =>
          endTutorial(event.getPlayer)
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
    player.sendMessage(new ComponentBuilder("Finished the tutorial").color(ChatColor.GREEN).create(): _*)

    val loc = Location.builder.copy(player.getWorld.getConfig.spawnPoint)
    loc.setWorld(player.getWorld)
    Teleporter.teleportPlayer(player, new TeleportOptions(loc.build()))

    removeWorldItemFromPlayer(player, "Tutorial:NextStage")
    removeWorldItemFromPlayer(player, "Tutorial:End")

    tutorialStage.remove(player)

    //player.setInventorySlot(0, getStartTutorialItem) //TODO

    player.setAllowedToFly(false)
  }

  private def doStageAction(player: Player){
    val nextStage = tutorialStage.getOrElse(player, 0)
    val stages = player.getMap.mappack.getMetadata.tutorial.stages
    if(nextStage >= stages.length){
      endTutorial(player)
    }else{
      val stage = stages(nextStage)
      val loc = Location.builder().copy(stage.teleport)
      loc.setWorld(player.getWorld)
      player.setAllowedToFly(true)
      Teleporter.teleportPlayer(player, new TeleportOptions(loc.build()))
      player.sendMessage(new TextComponent(""))
      player.sendMessage(new ComponentBuilder("-- " + stage.title).color(ChatColor.DARK_AQUA).create(): _*)
      for(line <- stage.messages) player.sendMessage(new ComponentBuilder(line).color(ChatColor.GRAY).create(): _*)
    }
  }

  private def removeWorldItemFromPlayer(player: Player, typ: String){
    var i = 0
    /*player.iterateInventory { s =>
      if(s != null && s.getTag("WorldItemType").isDefined && s.getTag("WorldItemType").get == typ) player.setInventorySlot(i, null)
      i += 1
    }*/ //TODO
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
    val stack = event.getStack
    if(stack != null && stack.getTag("WorldItemType") != null){
      event.setCanceled(true)
    }
  }
}
