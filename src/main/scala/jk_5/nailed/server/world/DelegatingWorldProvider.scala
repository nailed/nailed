package jk_5.nailed.server.world

import jk_5.nailed.api.world
import jk_5.nailed.server.world.void.{ChunkProviderVoid, WorldChunkManagerVoid}
import net.minecraft.world.WorldProvider
import net.minecraft.world.biome.{BiomeGenBase, WorldChunkManager, WorldChunkManagerHell}
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen._

/**
 * No description given
 *
 * @author jk-5
 */
class DelegatingWorldProvider(private val wrapped: world.WorldProvider) extends WorldProvider {

  private var dimId: Int = _

  override def getDimensionName = "DIM" + wrapped.getId

  override def setDimension(dimensionId: Int){
    dimId = dimensionId
    wrapped.setId(dimensionId)
    super.setDimension(dimensionId)
  }

  override def createChunkGenerator(): IChunkProvider = {
    if(wrapped.getType == "overworld"){
      new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed, this.worldObj.getWorldInfo.isMapFeaturesEnabled)
    }else if(wrapped.getType == "void"){
      new ChunkProviderVoid(this.worldObj)
    }else if(wrapped.getType == "nether"){
      new ChunkProviderHell(this.worldObj, this.worldObj.getSeed)
    }else if(wrapped.getType == "end"){
      new ChunkProviderEnd(this.worldObj, this.worldObj.getSeed)
    }else if(wrapped.getType == "flat"){
      new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed, this.worldObj.getWorldInfo.isMapFeaturesEnabled, this.wrapped.getOptions)
    }else throw new IllegalArgumentException("Unknown world type " + wrapped.getType)
  }

  override protected def registerWorldChunkManager(){
    if(wrapped.getType == "overworld"){
      this.worldChunkMgr = new WorldChunkManager(this.worldObj)
    }else if(wrapped.getType == "void"){
      this.worldChunkMgr = new WorldChunkManagerVoid(this.worldObj)
    }else if(wrapped.getType == "nether"){
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F)
      this.isHellWorld = true
      this.hasNoSky = true
    }else if(wrapped.getType == "end"){
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F)
      this.hasNoSky = true
    }else if(wrapped.getType == "flat"){
      val info = FlatGeneratorInfo.createFlatGeneratorFromString(this.wrapped.getOptions)
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.getBiome(info.getBiome), 0.5F)
    }else throw new IllegalArgumentException("Unknown world type " + wrapped.getType)
  }
}
