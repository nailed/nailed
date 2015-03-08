package jk_5.nailed.server.teleport

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.teleport.{TeleportEventEnd, TeleportEventStart}
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.nbt.NBTTagList

object MapInventoryListener {

  @EventHandler
  def onTeleportStart(event: TeleportEventStart){
    if(event.getNewWorld.getMap != event.getOldWorld.getMap){
      val map = event.getOldWorld.getMap.asInstanceOf[NailedMap]
      val player = event.getPlayer.asInstanceOf[NailedPlayer]
      val entity = player.getEntity
      if(map == null){
        entity.inventory.clear()
      }else{
        val nbt = new NBTTagList
        entity.inventory.writeToNBT(nbt)
        map.inventories.put(player, nbt)
        entity.inventory.clear()
      }
    }
  }

  @EventHandler
  def onTeleportEnd(event: TeleportEventEnd){
    if(event.getNewWorld.getMap != event.getOldWorld.getMap){
      val player = event.getPlayer.asInstanceOf[NailedPlayer]
      val entity = player.getEntity
      val map = event.getNewWorld.getMap.asInstanceOf[NailedMap]
      if(map != null){
        map.inventories.get(player) match {
          case Some(nbt) => entity.inventory.readFromNBT(nbt)
          case None =>
        }
      }
    }
  }
}
