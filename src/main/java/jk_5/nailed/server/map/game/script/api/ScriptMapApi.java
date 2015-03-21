package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.map.stat.ModifiableStat;
import jk_5.nailed.api.map.stat.Stat;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.TitleMessage;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.map.NailedMap;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.world.NailedWorld;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.List;

public final class ScriptMapApi {

    private final NailedMap map;
    private final Context context;
    private final ScriptableObject scope;
    final ScriptScoreboardApi scoreboard;

    public ScriptMapApi(NailedMap map, Context context, ScriptableObject scope) {
        this.map = map;
        this.context = context;
        this.scope = scope;
        this.scoreboard = new ScriptScoreboardApi(this, map);
    }

    public ScriptScoreboardApi getScoreboard(){
        return this.scoreboard;
    }

    public void sendChat(String msg){
        map.broadcastChatMessage(new TextComponent(msg));
    }

    public void sendChat(BaseComponent ... comp){
        map.broadcastChatMessage(comp);
    }

    public ScriptPlayerApi[] getPlayers(){
        List<ScriptPlayerApi> builder = new ArrayList<ScriptPlayerApi>();
        for (Player p : map.players()) {
            builder.add(new ScriptPlayerApi((NailedPlayer) p));
        }
        return builder.toArray(new ScriptPlayerApi[builder.size()]);
    }

    public ScriptWorldApi[] getWorlds(){
        List<ScriptWorldApi> builder = new ArrayList<ScriptWorldApi>();
        for (World w : map.worlds()) {
            builder.add(new ScriptWorldApi((NailedWorld) w));
        }
        return builder.toArray(new ScriptWorldApi[builder.size()]);
    }

    public ScriptWorldApi getWorld(String name){
        World world = null;
        for (World w : map.worlds()) {
            if(w.getConfig().name().equals(name)){
                world = w;
            }
        }
        if(world == null){
            return null;
        }
        return new ScriptWorldApi((NailedWorld) world);
    }

    public ScriptTeamApi getTeam(String name){
        return new ScriptTeamApi(map.getTeam(name), context, scope);
    }

    public void setUnreadyInterrupt(boolean unreadyInterrupt){
        map.getGameManager().setUnreadyInterrupt(unreadyInterrupt);
    }

    public void setWinInterrupt(boolean winInterrupt){
        map.getGameManager().setWinInterrupt(winInterrupt);
    }

    public void countdown(int seconds){
        TitleMessage.Builder builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(1).setFadeOutTime(30);
        int ellapsed = 0;
        do{
            TextComponent comp = new TextComponent(String.valueOf(seconds - ellapsed));
            if(seconds - ellapsed == 5) comp.setColor(ChatColor.YELLOW);
            if(seconds - ellapsed == 4) comp.setColor(ChatColor.GOLD);
            if(seconds - ellapsed <= 3) comp.setColor(ChatColor.RED);
            TitleMessage msg = builder.setTitle(comp).build();
            for (Player player : map.players()) {
                player.displayTitle(msg);
            }
            ellapsed += 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }while(ellapsed < seconds);
        TextComponent comp = new TextComponent("GO");
        comp.setColor(ChatColor.GREEN);
        TitleMessage msg = builder.setTitle(comp).build();
        for (Player player : map.players()) {
            player.displayTitle(msg);
        }
    }

    public void broadcastSubtitle(String msg){
        TextComponent comp = new TextComponent(msg);
        for (Player player : map.players()) {
            player.displaySubtitle(comp);
        }
    }

    public void broadcastSubtitle(BaseComponent comp){
        for (Player player : map.players()) {
            player.displaySubtitle(comp);
        }
    }

    public void broadcastSubtitle(BaseComponent ... comp){
        for (Player player : map.players()) {
            player.displaySubtitle(comp);
        }
    }

    public void setSubtitle(String msg){
        TextComponent comp = new TextComponent(msg);
        for (Player player : map.players()) {
            player.setSubtitle(comp);
        }
    }

    public void setSubtitle(BaseComponent comp){
        for (Player player : map.players()) {
            player.setSubtitle(comp);
        }
    }

    public void setSubtitle(BaseComponent ... comp){
        for (Player player : map.players()) {
            player.setSubtitle(comp);
        }
    }

    public void clearSubtitle(){
        for (Player player : map.players()) {
            player.clearSubtitle();
        }
    }

    public void enableStat(String name){
        Stat stat = map.getStatManager().getStat(name);
        if(stat != null && stat instanceof ModifiableStat){
            ((ModifiableStat) stat).enable();
        }
    }
    public void disableStat(String name){
        Stat stat = map.getStatManager().getStat(name);
        if(stat != null && stat instanceof ModifiableStat){
            ((ModifiableStat) stat).disable();
        }
    }

    public void setWinner(Object winner){
        if(winner instanceof ScriptPlayerApi){
            map.getGameManager().setWinner(NailedPlatform.instance().getPlayerByName(((ScriptPlayerApi) winner).getName()));
        }else if(winner instanceof ScriptTeamApi){
            map.getGameManager().setWinner(map.getTeam(((ScriptTeamApi) winner).getId()));
        }
    }
}
