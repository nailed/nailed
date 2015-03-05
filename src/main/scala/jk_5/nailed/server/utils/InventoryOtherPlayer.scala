package jk_5.nailed.server.utils

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.InventoryBasic

class InventoryOtherPlayer(private val owner: EntityPlayerMP, private val viewer: EntityPlayerMP) extends InventoryBasic(owner.getCommandSenderName + "\'s inventory", false, owner.inventory.mainInventory.length) {

  private var allowUpdate = false

  override def openInventory(player: EntityPlayer){
    InvSeeTicker.register(this)
    allowUpdate = false
    for(i <- 0 until owner.inventory.mainInventory.length){
      this.setInventorySlotContents(i, this.owner.inventory.mainInventory(i))
    }
    allowUpdate = true
    super.openInventory(player)
  }

  override def closeInventory(player: EntityPlayer){
    InvSeeTicker.unregister(this)
    if(allowUpdate){
      for(i <- 0 until owner.inventory.mainInventory.length){
        owner.inventory.mainInventory(i) = getStackInSlot(i)
      }
    }
    this.markDirty()
    super.closeInventory(player)
  }

  override def markDirty(){
    super.markDirty()
    if(allowUpdate){
      for(i <- 0 until owner.inventory.mainInventory.length){
        this.owner.inventory.mainInventory(i) = this.getStackInSlot(i)
      }
    }
  }

  def tick(){
    allowUpdate = false
    for(i <- 0 until owner.inventory.mainInventory.length){
      setInventorySlotContents(i, this.owner.inventory.mainInventory(i))
    }
    allowUpdate = true
    markDirty()
  }

  def getOwner = owner
  def getInventoryName = owner.getCommandSenderName + "\'s inventory"
}
