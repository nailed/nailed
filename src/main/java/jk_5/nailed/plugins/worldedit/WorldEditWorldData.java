package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.world.registry.BiomeRegistry;
import com.sk89q.worldedit.world.registry.LegacyWorldData;

/**
 * World data for the Forge platform.
 */
class WorldEditWorldData extends LegacyWorldData {

    private static final WorldEditWorldData INSTANCE = new WorldEditWorldData();
    private final BiomeRegistry biomeRegistry = new WorldEditBiomeRegistry();

    /**
     * Create a new instance.
     */
    WorldEditWorldData() {
    }

    @Override
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }

    /**
     * Get a static instance.
     *
     * @return an instance
     */
    public static WorldEditWorldData instance() {
        return INSTANCE;
    }
}
