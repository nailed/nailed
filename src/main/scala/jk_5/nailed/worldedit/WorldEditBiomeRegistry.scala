package jk_5.nailed.worldedit

import java.util

import com.sk89q.worldedit.world.biome.{BaseBiome, BiomeData}
import com.sk89q.worldedit.world.registry.BiomeRegistry
import net.minecraft.world.biome.BiomeGenBase

import scala.collection.mutable

/**
 * Provides access to biome data in Forge.
 *
 * @author jk-5
 */
object WorldEditBiomeRegistry extends BiomeRegistry {

  private var biomes = mutable.HashMap[Int, BiomeGenBase]()
  private var biomeData = mutable.HashMap[Int, BiomeData]()

  private[worldedit] def populate(){
    val biomes = mutable.HashMap[Int, BiomeGenBase]()
    val biomeData = mutable.HashMap[Int, BiomeData]()
    for(biome <- BiomeGenBase.biomeList){
      if(biome != null && !biomes.exists(_._2 == biome)){
        biomes.put(biome.biomeID, biome)
        biomeData.put(biome.biomeID, new CachedBiomeData(biome))
      }
    }
    WorldEditBiomeRegistry.biomes = biomes
    WorldEditBiomeRegistry.biomeData = biomeData
  }

  private class CachedBiomeData(val biome: BiomeGenBase) extends BiomeData {
    override def getName = biome.biomeName
  }

  override def createFromId(id: Int) = new BaseBiome(id)
  override def getBiomes: util.List[BaseBiome] = {
    val ret = new util.ArrayList[BaseBiome]()
    for(biome <- biomes.keysIterator){
      ret.add(new BaseBiome(biome))
    }
    ret
  }
  override def getData(biome: BaseBiome): BiomeData = biomeData.get(biome.getId).orNull
}
