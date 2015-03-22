package jk_5.nailed.server.scoreboard;

import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.scoreboard.Objective;
import jk_5.nailed.api.scoreboard.Score;
import jk_5.nailed.api.util.Checks;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NailedObjective implements Objective {

    private final String id;
    private final NetworkedScoreboardManager manager;
    private final Set<Score> scores = new HashSet<Score>();
    private final Map<String, Score> scoresByName = new HashMap<String, Score>();

    private String displayName;

    public NailedObjective(String id, NetworkedScoreboardManager manager) {
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
        Checks.check(displayName.length() <= 32, "displayName may not be longer than 32");

        this.displayName = displayName;
        S3BPacketScoreboardObjective packet = new S3BPacketScoreboardObjective();
        packet.objectiveName = this.id;
        packet.objectiveValue = this.displayName;
        packet.type = IScoreObjectiveCriteria.EnumRenderType.INTEGER; //TODO: config option
        packet.field_149342_c = 2; //Update
        this.manager.sendPacket(packet);
    }

    @Nonnull
    @Override
    public Score getScore(@Nonnull String name) {
        Checks.notNull(name, "name may not be null");
        Score score = this.scoresByName.get(name);
        if(score == null){
            score = new NailedScore(this, name);
            this.scores.add(score);
            this.scoresByName.put(name, score);
        }
        return score;
    }

    @Override
    public void removeScore(@Nonnull Score score) {
        Checks.notNull(score, "score may not be null");
        if(this.scores.remove(score)){
            this.scoresByName.remove(score.getName());
            S3CPacketUpdateScore p = new S3CPacketUpdateScore();
            p.name = score.getName();
            p.action = S3CPacketUpdateScore.Action.REMOVE;
            manager.sendPacket(p);
        }
    }

    public void sendData(Player player){
        for(Score score : this.scores){
            S3CPacketUpdateScore p = new S3CPacketUpdateScore();
            p.name = score.getName();
            p.objective = this.id;
            p.value = score.getValue();
            p.action = S3CPacketUpdateScore.Action.CHANGE;
            ((NailedPlayer) player).sendPacket(p);
        }
    }
}
