package jk_5.nailed.server.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.server.ServerPreTickEvent;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

public class InvSeeTicker {

    private static Multimap<EntityPlayer, InventoryOtherPlayer> map = HashMultimap.create();

    public static void register(InventoryOtherPlayer inv){
        map.put(inv.getOwner(), inv);
    }

    public static void unregister(InventoryOtherPlayer inv){
        map.remove(inv.getOwner(), inv);
    }

    @EventHandler
    public void onTick(ServerPreTickEvent event){
        for(Map.Entry<EntityPlayer, InventoryOtherPlayer> e : map.entries()){
            e.getValue().update();
        }
    }
}
