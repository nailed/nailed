package jk_5.nailed.server.utils

import com.google.common.collect.HashMultimap
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.server.ServerPreTickEvent
import net.minecraft.entity.player.EntityPlayer

import scala.collection.convert.wrapAsScala._

object InvSeeTicker {

  private val map = HashMultimap.create[EntityPlayer, InventoryOtherPlayer]()

  def register(inv: InventoryOtherPlayer) = map.put(inv.getOwner, inv)
  def unregister(inv: InventoryOtherPlayer) = map.remove(inv.getOwner, inv)

  @EventHandler
  def onTick(event: ServerPreTickEvent){
    map.entries().foreach(_.getValue.tick())
  }
}
