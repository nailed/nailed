package jk_5.nailed.server.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;

public class InventoryOtherPlayer extends InventoryBasic {

    private final EntityPlayerMP viewer;
    private final EntityPlayerMP owner;
    private boolean allowUpdate;

    public InventoryOtherPlayer(EntityPlayerMP owner, EntityPlayerMP viewer) {
        super(owner.getCommandSenderName() + "'s inventory", false, owner.inventory.mainInventory.length);
        this.viewer = viewer;
        this.owner = owner;
    }

    @Override
    public void openInventory(EntityPlayer player){
        InvSeeTicker.register(this);
        allowUpdate = false;
        for(int id = 0; id < owner.inventory.mainInventory.length; ++id){
            setInventorySlotContents(id, owner.inventory.mainInventory[id]);
        }
        allowUpdate = true;
        super.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player){
        InvSeeTicker.unregister(this);
        if(allowUpdate){
            for(int id = 0; id < owner.inventory.mainInventory.length; ++id){
                owner.inventory.mainInventory[id] = getStackInSlot(id);
            }
        }
        markDirty();
        super.closeInventory(player);
    }

    @Override
    public void markDirty(){
        super.markDirty();
        if(allowUpdate){
            for(int id = 0; id < owner.inventory.mainInventory.length; ++id){
                owner.inventory.mainInventory[id] = getStackInSlot(id);
            }
        }
    }

    public void update(){
        allowUpdate = false;
        for(int id = 0; id < owner.inventory.mainInventory.length; ++id){
            setInventorySlotContents(id, owner.inventory.mainInventory[id]);
        }
        allowUpdate = true;
        markDirty();
    }

    public EntityPlayer getOwner(){
        return owner;
    }
}
