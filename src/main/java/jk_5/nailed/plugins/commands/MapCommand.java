package jk_5.nailed.plugins.commands;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import jk_5.nailed.api.Platform;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.Require;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.mappack.Mappack;

public class MapCommand {

    @Command(aliases = "load", desc = "Loads a new map and registers it to the system")
    @Require("admin")
    public void startgame(Platform platform, final CommandSender sender, Mappack mappack){
        final Future<Map> future = platform.getMapLoader().createMapFor(mappack);
        future.addListener(new FutureListener<Map>(){
            @Override
            public void operationComplete(Future<Map> mapFuture) throws Exception {
                //TODO: handle potential future failures
                ComponentBuilder builder = new ComponentBuilder("Map ").color(ChatColor.GREEN);
                //.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to go to this map")))
                //.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/goto " + future.get().defaultWorld().getDimensionId)) //TODO: teleport to default world
                builder.append(future.get().mappack().getMetadata().name()).append(" was loaded");
                sender.sendMessage(builder.create());
            }
        });
    }
}
