package jk_5.nailed.server.worlditems

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.event.{PlayerJoinMapEvent, PlayerLeaveMapEvent, PlayerRightClickItemEvent, PlayerThrowItemEvent}
import jk_5.nailed.api.material.{Material, ItemStack => NItemStack}
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.teleport.Teleporter
import jk_5.nailed.server.utils.ItemStackConverter._

/**
 * No description given
 *
 * @author jk-5
 */
object WorldItemEventHandler {

  //TODO:
  // This is quick and hacky code, just to get a proof-of-concept done.
  // A lot of these methods and events need to be moved to API

  @EventHandler
  def onPlayerJoinMap(event: PlayerJoinMapEvent){
    if(event.map.mappack.getMetadata.tutorial != null){
      val is = new NItemStack(Material.EMERALD)
      is.setDisplayName(ChatColor.RESET.toString + ChatColor.GOLD.toString + "Tutorial")
      is.addLore(ChatColor.RESET.toString + ChatColor.GRAY.toString + "Right click to start a tutorial")
      is.setTag("WorldItemType", "Tutorial")

      event.player.asInstanceOf[NailedPlayer].getEntity.inventory.setInventorySlotContents(0, is)
    }
  }

  @EventHandler
  def onPlayerLeaveMap(event: PlayerLeaveMapEvent){
    val ent = event.player.asInstanceOf[NailedPlayer].getEntity
    for(i <- 0 until ent.inventory.getSizeInventory){
      val is = ent.inventory.getStackInSlot(i)
      if(is != null && is.hasTagCompound && is.getTagCompound.hasKey("WorldItemType")){
        ent.inventory.setInventorySlotContents(i, null)
      }
    }
  }

  @EventHandler
  def onPlayerRightClickItem(event: PlayerRightClickItemEvent){
    val stack = event.stack
    if(stack != null && stack.getTag("WorldItemType").isDefined){
      stack.getTag("WorldItemType").get match {
        case "Tutorial" =>
          val player = event.player.asInstanceOf[NailedPlayer]
          val ent = player.getEntity
          player.setAllowedToFly(allowed = true)
          player.sendMessage(new ComponentBuilder("Starting tutorial").color(ChatColor.GREEN).create())

          for(i <- 0 until ent.inventory.getSizeInventory){
            val is = ent.inventory.getStackInSlot(i)
            if(is != null && is.hasTagCompound && is.getTagCompound.hasKey("WorldItemType") && is.getTagCompound.getString("WorldItemType") == "Tutorial"){
              ent.inventory.setInventorySlotContents(i, null)
            }
          }

          val stage = player.getMap.mappack.getMetadata.tutorial.stages(0)

          val loc = new Location(stage.teleport)
          loc.setWorld(player.getWorld)
          Teleporter.teleportPlayer(player, new TeleportOptions(loc))
          player.sendMessage(new ComponentBuilder("-- " + stage.title).color(ChatColor.DARK_AQUA).create())
          for(line <- stage.messages) player.sendMessage(new ComponentBuilder(line).color(ChatColor.GRAY).create())
        case _ =>
      }
    }
  }

  @EventHandler
  def onPlayerThrowItem(event: PlayerThrowItemEvent){
    val stack = event.stack
    if(stack != null && stack.getTag("WorldItemType").isDefined){
      event.setCanceled(true)
    }
  }
}
