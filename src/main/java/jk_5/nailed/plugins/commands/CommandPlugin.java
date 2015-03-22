package jk_5.nailed.plugins.commands;

import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.Platform;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.Require;
import jk_5.nailed.api.command.parametric.annotation.Optional;
import jk_5.nailed.api.command.parametric.annotation.Text;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.sender.WorldCommandSender;
import jk_5.nailed.api.event.RegisterCommandsEvent;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.plugin.Plugin;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.WeatherType;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.utils.InventoryOtherPlayer;
import jk_5.nailed.server.utils.NBTUtils;
import net.minecraft.command.CommandEffect;
import net.minecraft.command.CommandExecuteAt;
import net.minecraft.command.CommandGive;
import net.minecraft.command.CommandHelp;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.command.server.CommandTestFor;
import net.minecraft.command.server.CommandTestForBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

@Plugin(id = "Nailed|Commands", name = "Nailed Commands", version = "1.0.0")
public class CommandPlugin {

    @EventHandler
    public void registerCommands(RegisterCommandsEvent event){
        event.registerCommandClass(this);
        event.subcommand("team").registerCommandClass(new TeamCommand());
        event.subcommand("nailed").registerCommandClass(new NailedCommand());
        event.subcommand("reload").registerCommandClass(new ReloadCommand());
        event.subcommand("game").registerCommandClass(new GameCommand());
        event.subcommand("map").registerCommandClass(new MapCommand());

        event.registerCallable(new VanillaCommand(new CommandHelp()), "help");
        event.registerCallable(new VanillaCommand(new CommandGive()), "give");
        event.registerCallable(new VanillaCommand(new CommandTestForBlock()), "testforblock");
        event.registerCallable(new VanillaCommand(new CommandTestFor()), "testfor");
        event.registerCallable(new VanillaCommand(new CommandSetBlock()), "setblock");
        event.registerCallable(new VanillaCommand(new CommandEffect()), "effect");
        event.registerCallable(new VanillaCommand(new CommandTeleport()), "tp");
        event.registerCallable(new VanillaCommand(new CommandExecuteAt()), "execute");
    }

    @Command(aliases = {"gamemode", "gm"}, desc = "Change your gamemode", usage = "[mode] [target]")
    @Require("admin")
    public void gamemode(CommandSender sender, @Optional GameMode mode, @Optional Player target) throws CommandException {
        if(mode == null){
            if(sender instanceof Player){
                Player pl = (Player) sender;
                GameMode current = pl.getGameMode();
                if(current == GameMode.CREATIVE){
                    pl.setGameMode(GameMode.SURVIVAL);
                }else{
                    pl.setGameMode(GameMode.CREATIVE);
                }
            }else{
                throw new CommandException("Please specify a gamemode and player");
            }
        }else{
            if(target == null){
                if(sender instanceof Player){
                    ((Player) sender).setGameMode(mode);
                }else{
                    throw new CommandException("Please specify a player");
                }
            }else{
                target.setGameMode(mode);
                sender.sendMessage(new ComponentBuilder("Set gamemode to " + mode.getName()).color(ChatColor.GREEN).create());
            }
        }
    }

    @Command(aliases = "difficulty", desc = "Change the world difficulty")
    @Require("admin")
    public void difficulty(WorldCommandSender sender, Difficulty difficulty){
        sender.getWorld().setDifficulty(difficulty);
        sender.sendMessage(new ComponentBuilder("Set difficulty to " + difficulty.getName()).color(ChatColor.GREEN).create());
    }

    @Command(aliases = "heal", desc = "Heal yourself or another player")
    @Require("admin")
    public void heal(CommandSender sender, @Optional Player target) throws CommandException {
        if(target == null){
            if(sender instanceof Player){
                ((Player) sender).setHealth(((Player) sender).getMaxHealth());
            }else{
                throw new CommandException("Please specify a player");
            }
        }else{
            target.setHealth(target.getMaxHealth());
        }
    }

    @Command(aliases = "kick", desc = "Kick a player")
    @Require("admin")
    public void kick(Platform platform, CommandSender sender, Player target, @Optional @Text String r){
        String reason = r == null ? "No reason given" : r;
        target.kick("Kicked by " + sender.getName() + ". Reason: " + reason);

        TextComponent b = new TextComponent("");
        b.setColor(ChatColor.RED);
        b.addExtra("Player ");
        b.addExtra(target.getDescriptionComponent());
        b.addExtra(" was kicked by ");
        b.addExtra(sender.getDescriptionComponent());
        platform.broadcastMessage(b);
        platform.broadcastMessage(new ComponentBuilder("Reason: " + reason).color(ChatColor.RED).create());

        TextComponent m = new TextComponent("Successfully kicked player ");
        m.addExtra(target.getDescriptionComponent());
        m.setColor(ChatColor.GREEN);
        sender.sendMessage(m);
    }

    @Command(aliases = "toggledownfall", desc = "Toggles rain")
    @Require("admin")
    public void toggledownfall(WorldCommandSender sender){
        World world = sender.getWorld();
        if(world.getWeather().isRaining()){
            world.setWeather(WeatherType.DRY);
            sender.sendMessage(new ComponentBuilder("Weather changed to dry").color(ChatColor.GREEN).create());
        }else{
            world.setWeather(WeatherType.RAIN);
            sender.sendMessage(new ComponentBuilder("Weather changed to raining").color(ChatColor.GREEN).create());
        }
    }

    @Command(aliases = "weather", desc = "Changes the weather")
    @Require("admin")
    public void weather(WorldCommandSender sender, WeatherType type){
        World world = sender.getWorld();
        world.setWeather(type);
        sender.sendMessage(new ComponentBuilder("Weather changed to " + type.getName()).color(ChatColor.GREEN).create());
    }

    @Command(aliases = "kill", desc = "Kills a player")
    @Require("admin")
    public void kill(CommandSender sender, Player target){
        target.setHealth(0);
        sender.sendMessage(new ComponentBuilder("Killed " + target.getName()).color(ChatColor.GREEN).create());
    }

    @Command(aliases = "goto", desc = "Teleports the player to a world")
    public void startgame(Platform platform, CommandSender sender, int dimension){
        if(sender instanceof Player){
            ((Player) sender).teleportTo(platform.getWorld(dimension));
        }
    }

    @Command(aliases = "statemitter", desc = "Gives you an stat emitter")
    @Require("admin")
    public void startgame(CommandSender sender, @Optional String statName) throws CommandException {
        NailedPlayer p;
        if(sender instanceof Player){
            p = ((NailedPlayer) sender);
        }else{
            throw new CommandException("You are not a player");
        }
        ItemStack is = new ItemStack(Blocks.command_block, 1);
        NBTUtils.getItemNBT(is).setBoolean("IsStatemitter", true);
        NBTUtils.setDisplayName(is, ChatColor.RESET + "Stat Emitter");
        if(statName != null){
            NBTUtils.getItemNBT(is).setString("Content", statName);
            NBTUtils.addLore(is, statName);
        }
        p.getEntity().inventory.addItemStackToInventory(is);
    }

    @Command(aliases = "invsee", desc = "Look at the inventory of another player")
    @Require("admin")
    public void invsee(CommandSender sender, Player player) throws CommandException {
        NailedPlayer p;
        if(sender instanceof Player){
            p = ((NailedPlayer) sender);
        }else{
            throw new CommandException("You are not a player");
        }
        EntityPlayerMP entity = p.getEntity();
        if(entity.openContainer != entity.inventoryContainer){
            entity.closeScreen();
        }
        entity.getNextWindowId();

        InventoryOtherPlayer chest = new InventoryOtherPlayer(((NailedPlayer) player).getEntity(), entity);
        entity.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(entity.currentWindowId, "minecraft:container", chest.getDisplayName(), chest.getSizeInventory()));
        entity.openContainer = new ContainerChest(entity.inventory, chest, entity);
        entity.openContainer.windowId = entity.currentWindowId;
        entity.openContainer.onCraftGuiOpened(entity);
    }

    @Command(aliases = {"teamspeak", "ts"}, desc = "Information about the teamspeak server")
    public void teamspeak(CommandSender sender, @Optional String tsName){

    }
}
