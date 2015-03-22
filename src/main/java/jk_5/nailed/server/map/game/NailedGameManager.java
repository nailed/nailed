package jk_5.nailed.server.map.game;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.map.*;
import jk_5.nailed.api.map.stat.StatEvent;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.TitleMessage;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.map.game.script.ScriptingEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NailedGameManager implements GameManager {

    private final Map map;
    private final ScriptingEngine scriptingEngine = new ScriptingEngine(this);

    private boolean isGameRunning = false;
    private boolean winInterrupt = false;
    private boolean unreadyInterrupt = false;
    private GameWinnable winner;

    public NailedGameManager(Map map) {
        this.map = map;
    }

    @Override
    @Nonnull
    public GameStartResult startGame() {
        if(isGameRunning){
            return new GameStartResult(false, "A game is already running");
        }
        try{
            List<Player> missingPlayers = new ArrayList<Player>();
            for (Team team : map.getTeams()) {
                for (Player member : team.members()) {
                    if(member.getMap() != map){
                        missingPlayers.add(member);
                    }
                }
            }
            if(missingPlayers.size() > 0){
                List<String> missingNames = new ArrayList<String>();
                for (Player player : missingPlayers) {
                    missingNames.add(player.getName());
                }
                String names = Joiner.on(", ").join(missingNames);
                return new GameStartResult(false, "Game could not be started because the following players are not in the map: " + names);
            }
            boolean scriptStarted = scriptingEngine.start();
            if(hasCustomGameType()){
                getGameType().onGameStarted(map);
            }
            isGameRunning = scriptStarted || hasCustomGameType();
            if(!isGameRunning){
                return new GameStartResult(false, "game.js could not be found or read");
            }
            map.getStatManager().fireEvent(new StatEvent("gameRunning", true));
            return new GameStartResult(true, null);
        }catch(Exception e){
            return new GameStartResult(false, "Exception while starting game", e);
        }
    }

    @Override
    public boolean endGame() {
        if(!isGameRunning){
            return false;
        }
        isGameRunning = false;
        if(hasCustomGameType()){
            getGameType().onGameEnded(map);
        }
        scriptingEngine.kill();
        map.getStatManager().fireEvent(new StatEvent("gameRunning", false));
        cleanup();
        return true;
    }

    public void onEnded(boolean success){
        if(!isGameRunning){
            return;
        }
        isGameRunning = false;
        if(this.hasCustomGameType()){
            getGameType().onGameEnded(map);
        }
        map.getStatManager().fireEvent(new StatEvent("gameRunning", false));
        cleanup();
    }

    @Override
    public void setWinner(@Nonnull GameWinnable winner) {
        if(this.winner != null){
            return;
        }
        this.winner = winner;
        TitleMessage.Builder builder = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40);
        builder.setTitle(new ComponentBuilder("You Win!").color(ChatColor.GREEN).create());
        if(winner instanceof Player){
            Player p = (Player) winner;
            p.displayTitle(builder.build());
            TitleMessage.Builder b2 = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40);
            TitleMessage t2 = b2.setTitle(new ComponentBuilder("You lost!").color(ChatColor.RED).create()).build();
            for (Player player : map.players()) {
                if(player != p){
                    player.displayTitle(t2);
                }
            }
        }else if(winner instanceof Team){
            Team t = (Team) winner;
            TitleMessage title = builder.build();
            for (Player player : t.members()) {
                player.displayTitle(title);
            }
            TitleMessage.Builder b2 = TitleMessage.builder().setFadeInTime(0).setDisplayTime(200).setFadeOutTime(40);
            TitleMessage t2 = b2.setTitle(new ComponentBuilder("You lost!").color(ChatColor.RED).create()).build();
            for (Team team : map.getTeams()) {
                if(team != t){
                    for (Player player : team.members()) {
                        player.displayTitle(t2);
                    }
                }
            }
            map.getStatManager().fireEvent(new StatEvent("teamWon", true, ImmutableMap.of("team", t.id())));
        }
        map.broadcastChatMessage(new ComponentBuilder(winner.getName() + " won the game").color(ChatColor.GOLD).create());
        onEnded(true);
        map.getStatManager().fireEvent(new StatEvent("gameHasWinner", true));
        if(winInterrupt){
            scriptingEngine.kill();
        }
    }

    @Override
    public boolean isGameRunning() {
        return false;
    }

    @Override
    public boolean hasCustomGameType() {
        return getGameType() != null;
    }

    @Nullable
    @Override
    public GameType getGameType() {
        if(map.mappack() != null){
            return NailedPlatform.instance().getGameTypeRegistry().getByName(map.mappack().getMetadata().gameType());
        }else{
            return null;
        }
    }

    @Override
    public void setWinInterrupt(boolean winInterrupt) {
        this.winInterrupt = winInterrupt;
    }

    @Override
    public boolean isWinInterrupt() {
        return this.winInterrupt;
    }

    @Override
    public void setUnreadyInterrupt(boolean unreadyInterrupt) {
        this.unreadyInterrupt = unreadyInterrupt;
    }

    @Override
    public boolean isUnreadyInterrupt() {
        return this.unreadyInterrupt;
    }

    private void cleanup(){
        for (Player player : map.players()) {
            player.clearSubtitle();
        }
    }

    public Map getMap() {
        return map;
    }
}
