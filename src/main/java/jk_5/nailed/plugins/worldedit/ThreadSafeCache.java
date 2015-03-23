package jk_5.nailed.plugins.worldedit;

import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.server.ServerPreTickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Caches data that cannot be accessed from another thread safely.
 */
public class ThreadSafeCache {

    private static final long REFRESH_DELAY = 1000 * 30;
    private static final ThreadSafeCache INSTANCE = new ThreadSafeCache();
    private Set<UUID> onlineIds = Collections.emptySet();
    private long lastRefresh = 0;

    /**
     * Get an concurrent-safe set of UUIDs of online players.
     *
     * @return a set of UUIDs
     */
    public Set<UUID> getOnlineIds() {
        return onlineIds;
    }

    @EventHandler
    public void tickStart(ServerPreTickEvent event) {
        long now = System.currentTimeMillis();

        if (now - lastRefresh > REFRESH_DELAY) {
            Set<UUID> onlineIds = new HashSet<UUID>();
            
            MinecraftServer server = MinecraftServer.getServer();
            if (server == null || server.getConfigurationManager() == null) {
                return;
            }
            for (Object object : server.getConfigurationManager().playerEntityList) {
                if (object != null) {
                    EntityPlayerMP player = (EntityPlayerMP) object;
                    onlineIds.add(player.getUniqueID());
                }
            }

            this.onlineIds = new CopyOnWriteArraySet<UUID>(onlineIds);

            lastRefresh = now;
        }
    }

    public static ThreadSafeCache getInstance() {
        return INSTANCE;
    }

}