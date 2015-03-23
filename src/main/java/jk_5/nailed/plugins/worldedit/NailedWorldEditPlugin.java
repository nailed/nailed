package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.util.command.CommandMapping;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.RegisterCommandsEvent;
import jk_5.nailed.api.event.player.PlayerInteractEvent;
import jk_5.nailed.api.event.plugin.RegisterAdditionalEventHandlersEvent;
import jk_5.nailed.api.plugin.Plugin;
import jk_5.nailed.api.plugin.PluginIdentifier;
import jk_5.nailed.server.player.NailedPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

@Plugin(id = "NailedWorldEditPlugin", version = "1.0.0", name = "NailedWorldEditPlugin")
public class NailedWorldEditPlugin {

    private static final Logger logger = LogManager.getLogger();

    private static NailedWorldEditPlugin INSTANCE;

    public Set<CommandMapping> commands;

    @PluginIdentifier.Instance
    public PluginIdentifier identifier;

    public NailedWorldEditPlugin() {
        INSTANCE = this;
    }

    @EventHandler
    public void registerEvents(RegisterAdditionalEventHandlersEvent event){
        event.register(ThreadSafeCache.getInstance());

        event.getPlatform().getMessenger().registerOutgoingPluginChannel(identifier, "WECUI");
        event.getPlatform().getMessenger().registerIncomingPluginChannel(identifier, "WECUI", new WorldEditCUIPacketHandler());

        WorldEditBiomeRegistry.populate();

        WorldEdit we = WorldEdit.getInstance();
        we.getPlatformManager().register(NailedWorldEditPlatform.instance());

        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }

    @EventHandler
    public void registerCommands(RegisterCommandsEvent event){
        for(final CommandMapping command : commands){
            WorldEditCommand wrapper = new WorldEditCommand(command);
            event.registerCallable(wrapper, command.getAllAliases());
        }
    }

    /*override def onDisable(){
        WorldEdit.getInstance.getPlatformManager.unregister(NailedPlatform)
    }*/

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(!NailedWorldEditPlatform.instance().isHookingEvents()){
            return;
        }
        WorldEdit we = WorldEdit.getInstance();
        WorldEditPlayer player = WorldEditPlayer.wrap(((NailedPlayer) event.getPlayer()).getEntity());
        WorldEditWorld world = WorldEditWorld.wrap(((NailedPlayer) event.getPlayer()).getEntity().worldObj);
        switch(event.getAction()){
            case LEFT_CLICK_AIR:
                if(we.handleArmSwing(player)){
                    event.setCanceled(true);
                }
                break;
            case LEFT_CLICK_BLOCK:
                com.sk89q.worldedit.util.Location location = new com.sk89q.worldedit.util.Location(world, event.getClicked().getX(), event.getClicked().getY(), event.getClicked().getZ());
                BlockInteractEvent e = new BlockInteractEvent(player, location, Interaction.HIT);
                we.getEventBus().post(e);
                if(e.isCancelled()){
                    event.setCanceled(true);
                }
                if(we.handleArmSwing(player)){
                    event.setCanceled(true);
                }
                break;
            case RIGHT_CLICK_AIR:
                if(we.handleRightClick(player)){
                    event.setCanceled(true);
                }
                break;
            case RIGHT_CLICK_BLOCK:
                com.sk89q.worldedit.util.Location loc = new com.sk89q.worldedit.util.Location(world, event.getClicked().getX(), event.getClicked().getY(), event.getClicked().getZ());
                BlockInteractEvent e1 = new BlockInteractEvent(player, loc, Interaction.OPEN);
                we.getEventBus().post(e1);
                if(e1.isCancelled()){
                    event.setCanceled(true);
                }
                if(we.handleRightClick(player)){
                    event.setCanceled(true);
                }
                break;
        }
    }

    public static NailedWorldEditPlugin instance(){
        return INSTANCE;
    }
}
