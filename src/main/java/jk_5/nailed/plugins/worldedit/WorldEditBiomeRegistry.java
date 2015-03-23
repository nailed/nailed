package jk_5.nailed.plugins.worldedit;

import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Provides access to biome data in Forge.
 */
class WorldEditBiomeRegistry implements BiomeRegistry {
    private static Map<Integer, BiomeGenBase> biomes = Collections.emptyMap();
    private static Map<Integer, BiomeData> biomeData = Collections.emptyMap();

    @Nullable
    @Override
    public BaseBiome createFromId(int id) {
        return new BaseBiome(id);
    }

    @Override
    public List<BaseBiome> getBiomes() {
        List<BaseBiome> list = new ArrayList<BaseBiome>();
        for (int biome : biomes.keySet()) {
            list.add(new BaseBiome(biome));
        }
        return list;
    }

    @Nullable
    @Override
    public BiomeData getData(BaseBiome biome) {
        return biomeData.get(biome.getId());
    }

    /**
     * Populate the internal static list of biomes.
     *
     * <p>If called repeatedly, the last call will overwrite all previous
     * calls.</p>
     */
    static void populate() {
        Map<Integer, BiomeGenBase> biomes = HashBiMap.create();
        Map<Integer, BiomeData> biomeData = new HashMap<Integer, BiomeData>();

        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if ((biome == null) || (biomes.containsValue(biome))) {
                continue;
            }
            biomes.put(biome.biomeID, biome);
            biomeData.put(biome.biomeID, new ForgeBiomeData(biome));
        }

        WorldEditBiomeRegistry.biomes = biomes;
        WorldEditBiomeRegistry.biomeData = biomeData;
    }

    /**
     * Cached biome data information.
     */
    private static class ForgeBiomeData implements BiomeData {
        private final BiomeGenBase biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private ForgeBiomeData(BiomeGenBase biome) {
            this.biome = biome;
        }

        @Override
        public String getName() {
            return biome.biomeName;
        }
    }

}