package jk_5.nailed.server.scoreboard;

import jk_5.nailed.api.scoreboard.Score;
import jk_5.nailed.api.util.Checks;
import net.minecraft.network.play.server.S3CPacketUpdateScore;

import javax.annotation.Nonnull;

public class NailedScore implements Score {

    private final NailedObjective owner;
    private final String name;

    private int value;

    public NailedScore(NailedObjective owner, String name) {
        this.owner = owner;
        this.name = name;

        Checks.notNull(owner, "owner may not be null");
        Checks.notNull(name, "name may not be null");
        Checks.check(name.length() <= 16, "name may not be longer than 16");
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
        this.update();
    }

    @Override
    public void addValue(int value) {
        this.setValue(this.value + value);
    }

    @Override
    public void update() {
        S3CPacketUpdateScore p = new S3CPacketUpdateScore();
        p.name = this.name;
        p.objective = this.owner.id();
        p.value = this.value;
        p.action = S3CPacketUpdateScore.Action.CHANGE;
        owner.manager().sendPacket(p);
    }
}
