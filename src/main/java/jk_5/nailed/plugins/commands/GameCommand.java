package jk_5.nailed.plugins.commands;

import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.Require;
import jk_5.nailed.api.command.sender.MapCommandSender;
import jk_5.nailed.api.command.sender.WorldCommandSender;
import jk_5.nailed.api.map.GameManager;
import jk_5.nailed.api.map.GameStartResult;
import jk_5.nailed.api.map.GameWinnable;
import jk_5.nailed.api.map.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameCommand {

    private static final Logger logger = LogManager.getLogger();

    @Command(aliases = "start", desc = "Starts the game in this map")
    @Require("admin")
    public void startgame(WorldCommandSender sender) throws CommandException {
        Map map = sender.getWorld().getMap();
        if(map == null) throw new CommandException("There is no game in this world");
        GameManager manager = map.getGameManager();
        GameStartResult startResult = manager.startGame();
        if(startResult.isSuccess()){
            sender.sendMessage(new ComponentBuilder("Started the game").color(ChatColor.GREEN).create());
        }else{
            if(startResult.getCause() != null){
                logger.info("Game in " + map + " could not be started because of an exception");
                logger.info(startResult.getError(), startResult.getCause());
            }
            throw new CommandException(startResult.getError(), startResult.getCause());
        }
    }

    @Command(aliases = "stop", desc = "Stops the game in this map")
    @Require("admin")
    public void stopgame(WorldCommandSender sender) throws CommandException {
        Map map = sender.getWorld().getMap();
        if(map == null) throw new CommandException("There is no game in this world");
        GameManager manager = map.getGameManager();
        if(!manager.isGameRunning()) throw new CommandException("No game is running");
        manager.endGame();
        sender.sendMessage(new ComponentBuilder("Ended the game").color(ChatColor.GREEN).create());
        map.broadcastChatMessage(new ComponentBuilder("The game was stopped by " + sender.getName()).color(ChatColor.GOLD).create());
    }

    @Command(aliases = "setwinner", desc = "Sets the winner")
    @Require("admin")
    public void stopgame(MapCommandSender sender, GameWinnable winner){
        sender.getMap().getGameManager().setWinner(winner);
    }
}
