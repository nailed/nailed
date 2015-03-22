package jk_5.nailed.server.scoreboard;

import jk_5.nailed.api.scoreboard.ScoreboardManager;
import net.minecraft.network.Packet;

public interface NetworkedScoreboardManager extends ScoreboardManager {

    void sendPacket(Packet packet);
}
