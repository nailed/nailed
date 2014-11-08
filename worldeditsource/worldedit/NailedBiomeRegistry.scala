package jk_5.nailed.worldedit

import java.util

import com.sk89q.worldedit.world.biome.{BaseBiome, BiomeData}
import com.sk89q.worldedit.world.registry.BiomeRegistry
import net.minecraft.world.biome.BiomeGenBase

import scala.collection.mutable

/**
 * Provides access to biome data in minecraft.
 *
 * @author jk-5
 */
object NailedBiomeRegistry {

  private var biomes = mutable.HashMap[Integer, BiomeGenBase]()
  private var biomeData = mutable.HashMap[Integer, BiomeData]()

  private[worldedit] def populate(){
    val biomes = mutable.HashMap[Integer, BiomeGenBase]()
    val biomeData = mutable.HashMap[Integer, BiomeData]()

    for(biome <- BiomeGenBase.biomeList){
      if(biome != null && !biomes.exists(_._2 == biome)){
        biomes.put(biome.biomeID, biome)
        biomeData.put(biome.biomeID, new NailedBiomeRegistry.CachedBiomeData(biome))
      }
    }
    NailedBiomeRegistry.biomes = biomes
    NailedBiomeRegistry.biomeData = biomeData
  }

  private case class CachedBiomeData(biome: BiomeGenBase) extends BiomeData {
    def getName = biome.biomeName
  }
}

class NailedBiomeRegistry extends BiomeRegistry {

  def createFromId(id: Int) = new BaseBiome(id)

  def getBiomes: util.List[BaseBiome] = {
    val list: util.List[BaseBiome] = new util.ArrayList[BaseBiome]
    for (biome <- NailedBiomeRegistry.biomes.keySet) {
      list.add(new BaseBiome(biome))
    }
    list
  }

  def getData(biome: BaseBiome): BiomeData = NailedBiomeRegistry.biomeData.get(biome.getId).orNull
}
