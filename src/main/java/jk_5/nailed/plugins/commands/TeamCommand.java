package jk_5.nailed.plugins.commands;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.Require;
import jk_5.nailed.api.command.sender.MapCommandSender;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.player.Player;

public final class TeamCommand {

    @Command(aliases = "join", desc = "Join a team")
    @Require("admin")
    public void difficulty(MapCommandSender sender, Player player, Team team){
        Map map = sender.getMap();
        map.setPlayerTeam(player, team);
        BaseComponent[] msg = new ComponentBuilder("Player ").color(ChatColor.GREEN).append(player.getName()).append(" is now in team ").append(team.name()).color(team.color()).create();
        map.broadcastChatMessage(msg);
    }

    @Command(aliases = "remove", desc = "Remove a player from its team")
    @Require("admin")
    public void difficulty(MapCommandSender sender, Player player){
        Map map = sender.getMap();
        Team team = map.getPlayerTeam(player);
        map.setPlayerTeam(player, null);
        BaseComponent[] msg = new ComponentBuilder("Player ").color(ChatColor.GREEN).append(player.getName()).append(" is no longer in team ").append(team.name()).color(team.color()).create();
        map.broadcastChatMessage(msg);
    }
}
