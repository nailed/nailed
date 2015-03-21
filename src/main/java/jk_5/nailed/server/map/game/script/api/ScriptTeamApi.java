package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.util.TitleMessage;
import jk_5.nailed.server.player.NailedPlayer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ScriptTeamApi {

    private final Team team;
    private final Context context;
    private final ScriptableObject scope;

    public ScriptTeamApi(Team team, Context context, ScriptableObject scope) {
        this.team = team;
        this.context = context;
        this.scope = scope;
    }

    @Nonnull
    public String getName() {
        return team.name();
    }

    @Nonnull
    public String getId() {
        return team.id();
    }

    @Nonnull
    public ChatColor getColor() {
        return team.color();
    }

    public ScriptPlayerApi[] getPlayers(){
        List<ScriptPlayerApi> builder = new ArrayList<ScriptPlayerApi>();
        for (Player player : team.members()) {
            builder.add(new ScriptPlayerApi((NailedPlayer) player));
        }
        return builder.toArray(new ScriptPlayerApi[builder.size()]);
    }

    public void forEachPlayer(Function function){
        for (Player player : team.members()) {
            function.call(context, scope, scope, new ScriptPlayerApi[]{new ScriptPlayerApi((NailedPlayer) player)});
        }
    }

    public void broadcastChat(String msg){
        TextComponent comp = new TextComponent(msg);
        for (Player player : team.members()) {
            player.sendMessage(comp);
        }
    }

    public void broadcastChat(BaseComponent ... comp){
        for (Player player : team.members()) {
            player.sendMessage(comp);
        }
    }

    public void broadcastSubtitle(String msg){
        TextComponent comp = new TextComponent(msg);
        for (Player player : team.members()) {
            player.displaySubtitle(comp);
        }
    }

    public void broadcastSubtitle(BaseComponent ... comp){
        for (Player player : team.members()) {
            player.displaySubtitle(comp);
        }
    }

    public void setSubtitle(String msg){
        TextComponent comp = new TextComponent(msg);
        for (Player player : team.members()) {
            player.setSubtitle(comp);
        }
    }

    public void setSubtitle(BaseComponent ... comp){
        for (Player player : team.members()) {
            player.setSubtitle(comp);
        }
    }

    public void clearSubtitle(){
        for (Player player : team.members()) {
            player.clearSubtitle();
        }
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
            for (Player player : team.members()) {
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
        for (Player player : team.members()) {
            player.displayTitle(msg);
        }
    }

    public void setSpawn(double x, double y, double z){
        team.setSpawnPoint(Location.builder().setX(x).setY(y).setZ(z).build());
    }

    public void resetSpawn(){
        team.setSpawnPoint(null);
    }
}
