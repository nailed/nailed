package jk_5.nailed.server.teleport;

import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.teleport.TeleportEventEnd;
import jk_5.nailed.api.event.teleport.TeleportEventStart;
import jk_5.nailed.server.map.NailedMap;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;

public class MapInventoryListener {

    @EventHandler
    public void onTeleportStart(TeleportEventStart event){
        if(event.getNewWorld().getMap() != event.getOldWorld().getMap()){
            NailedMap map = ((NailedMap) event.getOldWorld().getMap());
            NailedPlayer player = ((NailedPlayer) event.getPlayer());
            EntityPlayerMP entity = player.getEntity();
            if(map == null){
                entity.inventory.clear();
            }else{
                NBTTagList nbt = new NBTTagList();
                entity.inventory.writeToNBT(nbt);
                map.inventories.put(player, nbt);
                entity.inventory.clear();
            }
        }
    }

    @EventHandler
    public void onTeleportEnd(TeleportEventEnd event){
        if(event.getNewWorld().getMap() != event.getOldWorld().getMap()){
            NailedMap map = ((NailedMap) event.getOldWorld().getMap());
            NailedPlayer player = ((NailedPlayer) event.getPlayer());
            EntityPlayerMP entity = player.getEntity();
            if(map != null){
                NBTTagList nbt = map.inventories.get(player);
                if(nbt != null){
                    entity.inventory.readFromNBT(nbt);
                }
            }
        }
    }
}
