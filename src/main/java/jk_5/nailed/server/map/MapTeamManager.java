package jk_5.nailed.server.map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.mappack.metadata.MappackTeam;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.Checks;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.server.team.NailedTeam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MapTeamManager {

    private final NailedMap map;
    private final Map<Player, Team> playerTeams = new HashMap<Player, Team>();
    private final Map<String, Team> teams;
    private final Team defaultTeam = new Team() {
        private final Set<Player> members = new HashSet<Player>();

        @Nonnull
        @Override
        public String id() {
            return map.id() + ":default";
        }

        @Nonnull
        @Override
        public String name() {
            return "Default Team";
        }

        @Nonnull
        @Override
        public ChatColor color() {
            return ChatColor.WHITE;
        }

        @Nonnull
        @Override
        public Collection<Player> members() {
            return ImmutableSet.copyOf(members);
        }

        @Nullable
        @Override
        public Location getSpawnPoint() {
            return null;
        }

        @Override
        public void setSpawnPoint(@Nullable Location spawnpoint) {

        }

        @Override
        public String getName() {
            return name();
        }

        public void onPlayerJoined(Player player){
            members.add(player);
        }

        public void onPlayerLeft(Player player){
            members.remove(player);
        }
    };

    public MapTeamManager(@Nonnull NailedMap map) {
        this.map = map;

        if(this.map.mappack() != null){
            ImmutableMap.Builder<String, Team> teams = ImmutableMap.builder();
            for(MappackTeam team : this.map.mappack().getMetadata().teams()){
                teams.put(team.id(), new NailedTeam(team, this));
            }
            this.teams = teams.build();
        }else{
            this.teams = Collections.emptyMap();
        }
    }

    @Nullable
    public Team getTeam(String name) {
        return teams.get(name);
    }

    @Nullable
    public Team getPlayerTeam(Player player) {
        Team team = this.playerTeams.get(player);
        if(team == null){
            return defaultTeam;
        }else{
            return team;
        }
    }

    public void setPlayerTeam(@Nonnull Player player, @Nullable Team team) {
        Checks.notNull(player, "player may not be null");
        Team before = this.playerTeams.get(player);
        if(before != null){
            if(before instanceof NailedTeam){
                ((NailedTeam) before).onPlayerLeft(player);
            }
        }
        if(team == null || team == defaultTeam){
            this.playerTeams.remove(player);
        }else{
            this.playerTeams.put(player, team);
        }
        if(team != null){
            if(team instanceof NailedTeam){
                ((NailedTeam) team).onPlayerJoined(player);
            }
        }
    }

    @Nonnull
    public Collection<Team> getTeams() {
        return ImmutableSet.copyOf(teams.values());
    }

    public void onPlayerJoined(Player player) {

    }

    public void onPlayerLeft(Player player) {

    }

    public NailedMap getMap() {
        return map;
    }
}
