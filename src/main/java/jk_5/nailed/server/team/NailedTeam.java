package jk_5.nailed.server.team;

import com.google.common.collect.ImmutableSet;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.mappack.metadata.MappackTeam;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.scoreboard.ScoreboardTeam;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.server.map.MapTeamManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NailedTeam implements Team {

    private final MappackTeam mappackTeam;
    private final Set<Player> memberSet = new HashSet<Player>();
    private final ScoreboardTeam scoreboardTeam;
    private Location spawnpoint = null;

    public NailedTeam(MappackTeam mappackTeam, MapTeamManager manager) {
        this.mappackTeam = mappackTeam;
        this.scoreboardTeam = manager.getMap().getScoreboardManager().getOrCreateTeam(mappackTeam.id());
        this.scoreboardTeam.setPrefix(mappackTeam.color().toString());
        this.scoreboardTeam.setDisplayName(mappackTeam.name());
    }

    public void onPlayerJoined(@Nonnull Player player){
        this.memberSet.add(player);
        scoreboardTeam.addPlayer(player);
    }

    public void onPlayerLeft(@Nonnull Player player){
        this.memberSet.remove(player);
        this.scoreboardTeam.removePlayer(player);
    }

    @Nonnull
    @Override
    public String id() {
        return mappackTeam.id();
    }

    @Nonnull
    @Override
    public String name() {
        return mappackTeam.name();
    }

    @Nonnull
    @Override
    public ChatColor color() {
        return mappackTeam.color();
    }

    @Nonnull
    @Override
    public Collection<Player> members() {
        return ImmutableSet.copyOf(memberSet);
    }

    @Nullable
    @Override
    public Location getSpawnPoint() {
        return spawnpoint;
    }

    @Override
    public void setSpawnPoint(@Nullable Location spawnpoint) {
        this.spawnpoint = spawnpoint;
    }

    @Override
    public String getName() {
        return mappackTeam.name();
    }
}
