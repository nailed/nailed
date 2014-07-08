package jk_5.nailed.server.world

import jk_5.nailed.api.map.Map
import jk_5.nailed.api.world.{World => NWorld, WorldProvider => NWorldProvider}
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
class DelegatingWorldProvider(val wrapped: NWorldProvider) extends WorldProvider {

  private var dimId: Int = _
  private lazy val world: NWorld = NailedDimensionManager.getWorld(this.dimId)
  private lazy val map: Map = this.world.getMap.orNull

  override def getDimensionName = "DIM" + wrapped.getId

  override def setDimension(dimensionId: Int){
    dimId = dimensionId
    wrapped.setId(dimensionId)
    super.setDimension(dimensionId)
  }

  override def createChunkGenerator(): IChunkProvider = wrapped.getType match {
    case "overworld" => new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed, this.worldObj.getWorldInfo.isMapFeaturesEnabled)
    case "void" => new ChunkProviderVoid(this.worldObj)
    case "nether" => new ChunkProviderHell(this.worldObj, this.worldObj.getSeed)
    case "end" => new ChunkProviderEnd(this.worldObj, this.worldObj.getSeed)
    case "flat" => new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed, this.worldObj.getWorldInfo.isMapFeaturesEnabled, this.wrapped.getOptions)
    case _ => throw new IllegalArgumentException("Unknown world type " + wrapped.getType)
  }

  override protected def registerWorldChunkManager(): Unit = wrapped.getType match {
    case "overworld" => this.worldChunkMgr = new WorldChunkManager(this.worldObj)
    case "void" => this.worldChunkMgr = new WorldChunkManagerVoid(this.worldObj)
    case "nether" =>
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F)
      this.isHellWorld = true
      this.hasNoSky = true
    case "end" =>
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F)
      this.hasNoSky = true
    case "flat" =>
      val info = FlatGeneratorInfo.createFlatGeneratorFromString(this.wrapped.getOptions)
      this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.getBiome(info.getBiome), 0.5F)
    case _ => throw new IllegalArgumentException("Unknown world type " + wrapped.getType)
  }

  override def getSpawnPoint = this.world.getConfig.spawnPoint
}
