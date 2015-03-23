package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import io.netty.util.CharsetUtil;
import jk_5.nailed.api.messaging.PluginMessageListener;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.server.player.NailedPlayer;

public class WorldEditCUIPacketHandler implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(WorldEditPlayer.wrap(((NailedPlayer) player).getEntity()));
        if(session.hasCUISupport()){
            session.handleCUIInitializationMessage(new String(message, CharsetUtil.UTF_8));
        }
    }
}
