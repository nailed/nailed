package jk_5.nailed.server.teleport;

import jk_5.nailed.api.event.teleport.*;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.util.TeleportOptions;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.NailedEventFactory;

public class TeleportEventFactory {

    public static boolean isTeleportAllowed(World origin, World destination, Player entity, TeleportOptions options){
        return !NailedEventFactory.fireEvent(new TeleportEventAllow(origin, destination, entity, options.copy())).isCanceled();
    }

    public static Location alterDestination(World origin, World destination, Player entity, TeleportOptions options){
        Location newLoc = NailedEventFactory.fireEvent(new TeleportEventAlter(origin, destination, entity, options.copy())).getLocation();
        if(newLoc == null){
            return options.getDestination();
        }else{
            return newLoc;
        }
    }

    public static void onLinkStart(World origin, World destination, Player entity, TeleportOptions options){
        NailedEventFactory.fireEvent(new TeleportEventStart(origin, destination, entity, options.copy()));
    }

    public static void onExitWorld(World origin, World destination, Player entity, TeleportOptions options){
        NailedEventFactory.fireEvent(new TeleportEventExitWorld(origin, destination, entity, options.copy()));
    }

    public static void onEnterWorld(World origin, World destination, Player entity, TeleportOptions options){
        NailedEventFactory.fireEvent(new TeleportEventEnterWorld(origin, destination, entity, options.copy()));
    }

    public static void onEnd(World origin, World destination, Player entity, TeleportOptions options){
        NailedEventFactory.fireEvent(new TeleportEventEnd(origin, destination, entity, options.copy()));
    }
}
