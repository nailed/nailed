package jk_5.nailed.server.worlditems;

import gnu.trove.map.hash.TObjectIntHashMap;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.event.player.PlayerJoinMapEvent;
import jk_5.nailed.api.event.player.PlayerLeaveMapEvent;
import jk_5.nailed.api.event.player.PlayerRightClickItemEvent;
import jk_5.nailed.api.event.player.PlayerThrowItemEvent;
import jk_5.nailed.api.item.ItemStack;
import jk_5.nailed.api.item.Material;
import jk_5.nailed.api.mappack.metadata.TutorialStage;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.util.TeleportOptions;
import jk_5.nailed.server.teleport.Teleporter;

public class WorldItemEventHandler {

    private static final WorldItemEventHandler INSTANCE = new WorldItemEventHandler();

    //TODO:
    // Unmount before teleporting

    private TObjectIntHashMap<Player> tutorialStage = new TObjectIntHashMap<Player>();

    @EventHandler
    public void onPlayerJoinMap(PlayerJoinMapEvent event){
        tutorialStage.remove(event.getPlayer());
        if(event.getMap().mappack().getMetadata().tutorial() != null){
            //event.getPlayer.setInventorySlot(0, getStartTutorialItem) //TODO
            tutorialStage.put(event.getPlayer(), -1);
        }
    }

    @EventHandler
    public void onPlayerLeaveMap(PlayerLeaveMapEvent event){
        int i = 0;
        /*event.getPlayer.iterateInventory { s =>
          if(s != null && s.getTag("WorldItemType").isDefined) event.getPlayer.setInventorySlot(i, null)
          i += 1
        }*/ //TODO
        tutorialStage.remove(event.getPlayer());
        event.getPlayer().setAllowedToFly(false); //TODO don't set this to false if the player is creative or was allowed to fly
    }

    @EventHandler
    public void onPlayerRightClickItem(PlayerRightClickItemEvent event){
        ItemStack stack = event.getStack();
        if(stack != null && stack.getTag("WorldItemType") != null){
            String type = stack.getTag("WorldItemType");
            if(type.equals("Tutorial")){
                event.getPlayer().sendMessage(new ComponentBuilder("Starting tutorial").color(ChatColor.GREEN).create());

                removeWorldItemFromPlayer(event.getPlayer(), "Tutorial");

                //event.getPlayer.setInventorySlot(0, getNextStageItem) //TODO
                //event.getPlayer.setInventorySlot(8, getEndTutorialItem) //TODO

                nextStage(event.getPlayer());
                event.getPlayer().setAllowedToFly(true);
                doStageAction(event.getPlayer());
                event.getPlayer().setAllowedToFly(true);
            }else if(type.equals("Tutorial:NextStage")){
                nextStage(event.getPlayer());
                doStageAction(event.getPlayer());
            }else if(type.equals("Tutorial:End")){
                endTutorial(event.getPlayer());
            }
        }
    }

    private int nextStage(Player player){
        int current = tutorialStage.get(player);
        if(current == tutorialStage.getNoEntryValue()){
            current = -1;
        }
        int next = current + 1;
        tutorialStage.put(player, next);
        return next;
    }

    private void endTutorial(Player player){
        player.sendMessage(new TextComponent(""));
        player.sendMessage(new ComponentBuilder("Finished the tutorial").color(ChatColor.GREEN).create());

        Location.Builder loc = Location.builder().copy(player.getWorld().getConfig().spawnPoint());
        loc.setWorld(player.getWorld());
        Teleporter.teleportPlayer(player, new TeleportOptions(loc.build()));

        removeWorldItemFromPlayer(player, "Tutorial:NextStage");
        removeWorldItemFromPlayer(player, "Tutorial:End");

        tutorialStage.remove(player);

        //player.setInventorySlot(0, getStartTutorialItem) //TODO

        player.setAllowedToFly(false);
    }

    private void doStageAction(Player player){
        int nextStage = tutorialStage.get(player);
        if(nextStage == tutorialStage.getNoEntryValue()){
            nextStage = -1;
        }
        TutorialStage[] stages = player.getMap().mappack().getMetadata().tutorial().stages();
        if(nextStage >= stages.length){
            endTutorial(player);
        }else{
            TutorialStage stage = stages[nextStage];
            Location.Builder loc = Location.builder().copy(stage.teleport());
            loc.setWorld(player.getWorld());
            player.setAllowedToFly(true);
            Teleporter.teleportPlayer(player, new TeleportOptions(loc.build()));
            player.sendMessage(new TextComponent(""));
            player.sendMessage(new ComponentBuilder("-- " + stage.title()).color(ChatColor.DARK_AQUA).create());
            for(String line : stage.messages()){
                player.sendMessage(new ComponentBuilder(line).color(ChatColor.GRAY).create());
            }
        }
    }

    private void removeWorldItemFromPlayer(Player player, String type){
        int i = 0;
        /*player.iterateInventory { s =>
          if(s != null && s.getTag("WorldItemType").isDefined && s.getTag("WorldItemType").get == typ) player.setInventorySlot(i, null)
          i += 1
        }*/ //TODO
    }

    public ItemStack getStartTutorialItem(){
        ItemStack is = new ItemStack(Material.EMERALD);
        is.setDisplayName(ChatColor.RESET.toString() + ChatColor.GOLD.toString() + "Tutorial");
        is.addLore(ChatColor.RESET.toString() + ChatColor.GRAY.toString() + "Right click to start a tutorial");
        is.setTag("WorldItemType", "Tutorial");
        return is;
    }

    public ItemStack getNextStageItem(){
        ItemStack is = new ItemStack(Material.EMERALD);
        is.setDisplayName(ChatColor.RESET.toString() + ChatColor.GOLD.toString() + "Tutorial - Next Stage");
        is.addLore(ChatColor.RESET.toString() + ChatColor.GRAY.toString() + "Right click to go to the next tutorial stage");
        is.setTag("WorldItemType", "Tutorial:NextStage");
        return is;
    }

    public ItemStack getEndTutorialItem(){
        ItemStack is = new ItemStack(Material.WOOL, 1, (short) 14);
        is.setDisplayName(ChatColor.RESET.toString() + ChatColor.RED.toString() + "Stop Tutorial");
        is.addLore(ChatColor.RESET.toString() + ChatColor.GRAY.toString() + "Right click to stop the tutorial");
        is.setTag("WorldItemType", "Tutorial:End");
        return is;
    }

    @EventHandler
    public void onPlayerThrowItem(PlayerThrowItemEvent event){
        ItemStack stack = event.getStack();
        if(stack != null && stack.getTag("WorldItemType") != null){
            event.setCanceled(true);
        }
    }

    public static WorldItemEventHandler instance(){
        return INSTANCE;
    }
}
