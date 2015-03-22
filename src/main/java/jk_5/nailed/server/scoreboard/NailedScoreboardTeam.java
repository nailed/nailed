package jk_5.nailed.server.scoreboard;

import com.google.common.collect.ImmutableSet;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.scoreboard.ScoreboardTeam;
import jk_5.nailed.api.scoreboard.Visibility;
import jk_5.nailed.api.util.Checks;
import net.minecraft.network.play.server.S3EPacketTeams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NailedScoreboardTeam implements ScoreboardTeam {

    private final String id;
    private final NetworkedScoreboardManager manager;

    private String displayName;
    private String prefix = "";
    private String suffix = "";
    private boolean friendlyFire = true;
    private boolean friendlyInvisiblesVisible = false;
    private Set<Player> players = new HashSet<Player>();
    private Visibility nameTagVisibility = Visibility.ALWAYS;
    private Visibility deathMessageVisibility = Visibility.ALWAYS;

    public NailedScoreboardTeam(String id, NetworkedScoreboardManager manager) {
        this.id = id;
        this.manager = manager;
        this.displayName = id;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(@Nonnull String displayName) {
        Checks.notNull(displayName, "displayName may not be null");
        this.displayName = displayName;
        this.update();
    }

    @Nullable
    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        Checks.notNull(prefix, "prefix may not be null");
        this.prefix = prefix;
        this.update();
    }

    @Nullable
    @Override
    public String getSuffix() {
        return this.suffix;
    }

    @Override
    public void setSuffix(@Nullable String suffix) {
        Checks.notNull(suffix, "suffix may not be null");
        this.suffix = suffix;
        this.update();
    }

    @Override
    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    @Override
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        this.update();
    }

    @Override
    public boolean areFriendlyInvisiblesInvisible() {
        return this.friendlyInvisiblesVisible;
    }

    @Override
    public void setFriendlyInvisiblesVisible(boolean friendlyInvisiblesVisible) {
        this.friendlyInvisiblesVisible = friendlyInvisiblesVisible;
        this.update();
    }

    @Override
    public Visibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override
    public void setNameTagVisibility(Visibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
        this.update();
    }

    @Override
    public Visibility getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }

    @Override
    public void setDeathMessageVisibility(Visibility deathMessageVisibility) {
        this.deathMessageVisibility = deathMessageVisibility;
        this.update();
    }

    @Override
    public boolean addPlayer(Player player) {
        Checks.notNull(player, "player may not be null");
        if(players.add(player)){
            S3EPacketTeams packet = new S3EPacketTeams();
            packet.field_149320_a = id;
            packet.field_149317_e = getPlayerNames();
            packet.field_149314_f = 3; //Add Player
            manager.sendPacket(packet);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean removePlayer(Player player) {
        Checks.notNull(player, "player may not be null");
        if(players.remove(player)){
            S3EPacketTeams packet = new S3EPacketTeams();
            packet.field_149320_a = id;
            packet.field_149317_e = getPlayerNames();
            packet.field_149314_f = 4; //Remove Player
            manager.sendPacket(packet);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Collection<Player> getPlayers() {
        return ImmutableSet.copyOf(this.players);
    }

    @Override
    public Collection<String> getPlayerNames() {
        List<String> names = new ArrayList<String>();
        for (Player player : this.players) {
            names.add(player.getName());
        }
        return ImmutableSet.copyOf(names);
    }

    public void update(){
        int flags = 0;
        if(this.friendlyFire) flags |= 0x1;
        if(this.friendlyInvisiblesVisible) flags |= 0x2;
        //TODO: visibility?

        S3EPacketTeams packet = new S3EPacketTeams();
        packet.field_149320_a = this.id;
        packet.field_149318_b = this.displayName;
        packet.field_149319_c = this.prefix;
        packet.field_149316_d = this.suffix;
        packet.field_149317_e = getPlayerNames();
        packet.field_149314_f = 2; //Update
        packet.field_149315_g = flags;
        manager.sendPacket(packet);
    }
}
