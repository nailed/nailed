package jk_5.nailed.server.map;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.map.GameManager;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.map.stat.StatManager;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.scoreboard.ScoreboardManager;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.map.game.NailedGameManager;
import jk_5.nailed.server.map.stat.NailedStatManager;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.scoreboard.MapScoreboardManager;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class NailedMap implements Map {

    private final int id;
    private final Mappack mappack;
    private final File baseDir;
    private final Set<Player> players = new HashSet<Player>();
    private World defaultWorld;

    private final MapScoreboardManager scoreboardManager;
    private final MapTeamManager teamManager;
    private final NailedGameManager gameManager;
    private final NailedStatManager statManager;

    public final HashMap<Player, NBTTagList> inventories = new HashMap<Player, NBTTagList>();

    public NailedMap(int id, Mappack mappack, File baseDir) {
        this.id = id;
        this.mappack = mappack;
        this.baseDir = baseDir;

        this.scoreboardManager = new MapScoreboardManager(this);
        this.teamManager = new MapTeamManager(this);
        this.gameManager = new NailedGameManager(this);
        this.statManager = new NailedStatManager(this);
    }

    @Override
    public int id() {
        return this.id;
    }

    @Nonnull
    @Override
    public Collection<World> worlds() {
        List<World> worlds = NailedMapLoader.instance().getWorldsForMap(this);
        if(worlds != null){
            return ImmutableList.copyOf(worlds);
        }else{
            return Collections.emptyList();
        }
    }

    @Nonnull
    @Override
    public World[] worldsArray() {
        List<World> worlds = NailedMapLoader.instance().getWorldsForMap(this);
        if(worlds != null){
            return worlds.toArray(new World[worlds.size()]);
        }else{
            return new World[0];
        }
    }

    @Nonnull
    @Override
    public World defaultWorld() {
        return defaultWorld;
    }

    @Nullable
    @Override
    public Mappack mappack() {
        return mappack;
    }

    @Override
    public void addWorld(@Nonnull World world) {
        NailedMapLoader.instance().addWorldToMap(world, this);
        if(world.getConfig().isDefault()){
            defaultWorld = world;
        }
    }

    public void onPlayerJoined(Player player){
        players.add(player);
        scoreboardManager.onPlayerJoined(player);
        teamManager.onPlayerJoined(player);
    }

    public void onPlayerLeft(Player player){
        players.remove(player);
        scoreboardManager.onPlayerLeft(player);
        teamManager.onPlayerLeft(player);

        ((NailedPlayer) player).getEntity().fallDistance = 0;
        player.setGameMode(GameMode.ADVENTURE); //TODO: Maps default gamemode
        player.setAllowedToFly(false);
    }

    @Override
    public void broadcastChatMessage(BaseComponent... message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    @Nonnull
    @Override
    public Collection<Player> players() {
        return ImmutableSet.copyOf(players);
    }

    @Nonnull
    @Override
    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    @Nonnull
    @Override
    public GameManager getGameManager() {
        return this.gameManager;
    }

    @Nonnull
    @Override
    public StatManager getStatManager() {
        return this.statManager;
    }

    @Nullable
    @Override
    public Team getTeam(String name) {
        return teamManager.getTeam(name);
    }

    @Nullable
    @Override
    public Team getPlayerTeam(Player player) {
        return teamManager.getPlayerTeam(player);
    }

    @Override
    public void setPlayerTeam(@Nonnull Player player, @Nullable Team team) {
        teamManager.setPlayerTeam(player, team);
    }

    @Nonnull
    @Override
    public Collection<Team> getTeams() {
        return teamManager.getTeams();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("mappack", mappack)
                .add("baseDir", baseDir)
                .toString();
    }
}
