package jk_5.nailed.server.scoreboard;

import com.google.common.collect.ImmutableList;
import jk_5.nailed.api.scoreboard.DisplayType;
import jk_5.nailed.api.scoreboard.Objective;
import jk_5.nailed.api.scoreboard.ScoreboardManager;
import jk_5.nailed.api.scoreboard.ScoreboardTeam;
import jk_5.nailed.api.util.Checks;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PlayerScoreboardManager implements ScoreboardManager, NetworkedScoreboardManager {

    private final NailedPlayer player;
    private final Set<NailedObjective> objectives = new HashSet<NailedObjective>();
    private final Map<String, NailedObjective> objectivesById = new HashMap<String, NailedObjective>();
    private final Set<NailedScoreboardTeam> teams = new HashSet<NailedScoreboardTeam>();
    private final Map<String, NailedScoreboardTeam> teamsById = new HashMap<String, NailedScoreboardTeam>();
    private final Map<DisplayType, Objective> displayLocations = new EnumMap<DisplayType, Objective>(DisplayType.class);

    public PlayerScoreboardManager(NailedPlayer player) {
        this.player = player;
    }

    @Nonnull
    @Override
    public Objective getOrCreateObjective(@Nonnull String id) {
        Checks.notNull(id, "Id may not be null");
        Checks.check(id.length() <= 16, "Id must not be longer than 16");

        NailedObjective objective = objectivesById.get(id);
        if(objective == null){
            objective = new NailedObjective(id, this);
            this.objectives.add(objective);
            this.objectivesById.put(id, objective);
            S3BPacketScoreboardObjective packet = new S3BPacketScoreboardObjective();
            packet.objectiveName = objective.getId();
            packet.objectiveValue = objective.getDisplayName();
            packet.field_149342_c = 0; //0 = Create
            packet.type = IScoreObjectiveCriteria.EnumRenderType.INTEGER; //TODO: config option
            this.sendPacket(packet);
        }
        return objective;
    }

    @Nullable
    @Override
    public Objective getObjective(@Nonnull String id) {
        Checks.notNull(id, "Id may not be null");
        return objectivesById.get(id);
    }

    @Override
    public void setDisplay(@Nonnull DisplayType display, @Nullable Objective objective) {
        Checks.notNull(display, "Display type may not be null");
        //if(this.displayLocations.get(type) == objective){
        //    return;
        //}
        S3DPacketDisplayScoreboard packet = new S3DPacketDisplayScoreboard();
        packet.position = display.getId();
        if(objective == null){
            this.displayLocations.remove(display);
            packet.scoreName = "";
        }else{
            this.displayLocations.put(display, objective);
            packet.scoreName = objective.getId();
        }
        this.sendPacket(packet);
    }

    @Nonnull
    @Override
    public ScoreboardTeam getOrCreateTeam(@Nonnull String id) {
        Checks.notNull(id, "Id may not be null");

        NailedScoreboardTeam team = teamsById.get(id);
        if(team == null){
            team = new NailedScoreboardTeam(id, this);
            this.teamsById.put(id, team);
            this.teams.add(team);

            int flags = 0;
            if(team.isFriendlyFire()) flags |= 0x1;
            if(team.areFriendlyInvisiblesInvisible()) flags |= 0x2;
            //TODO: visibility

            S3EPacketTeams packet = new S3EPacketTeams();
            packet.field_149320_a = team.getId();
            packet.field_149318_b = team.getDisplayName();
            packet.field_149319_c = team.getPrefix();
            packet.field_149316_d = team.getSuffix();
            packet.field_149317_e = ImmutableList.of();
            packet.field_149314_f = 0; //Create
            packet.field_149315_g = flags;
            this.sendPacket(packet);
        }
        return team;
    }

    @Nullable
    @Override
    public ScoreboardTeam getTeam(@Nonnull String id) {
        Checks.notNull(id, "id may not be null");
        return this.teamsById.get(id);
    }

    @Override
    public void sendPacket(Packet packet) {
        player.sendPacket(packet);
    }

    public void onJoinedServer(){
        for (NailedObjective objective : this.objectives) {
            S3BPacketScoreboardObjective packet = new S3BPacketScoreboardObjective();
            packet.objectiveName = objective.getId();
            packet.objectiveValue = objective.getDisplayName();
            packet.field_149342_c = 0; //0 = Create
            packet.type = IScoreObjectiveCriteria.EnumRenderType.INTEGER; //TODO: config option
            player.sendPacket(packet);

            objective.sendData(player);
        }

        for (Map.Entry<DisplayType, Objective> e : this.displayLocations.entrySet()) {
            S3DPacketDisplayScoreboard packet = new S3DPacketDisplayScoreboard();
            packet.position = e.getKey().getId();
            packet.scoreName = e.getValue().getId();
            player.sendPacket(packet);
        }

        for (NailedScoreboardTeam team : this.teams) {
            int flags = 0;
            if(team.isFriendlyFire()) flags |= 0x1;
            if(team.areFriendlyInvisiblesInvisible()) flags |= 0x2;

            S3EPacketTeams packet = new S3EPacketTeams();
            packet.field_149320_a = team.getId();
            packet.field_149318_b = team.getDisplayName();
            packet.field_149319_c = team.getPrefix();
            packet.field_149316_d = team.getSuffix();
            packet.field_149317_e = team.getPlayerNames();
            packet.field_149314_f = 0; //Create
            packet.field_149315_g = flags;
            player.sendPacket(packet);
        }
    }

    public void onLeftServer(){

    }
}
