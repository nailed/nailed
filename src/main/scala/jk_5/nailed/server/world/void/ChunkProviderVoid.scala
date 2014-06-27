package jk_5.nailed.server.world.void

import net.minecraft.block.Block
import net.minecraft.world.World
import net.minecraft.world.chunk.{Chunk, IChunkProvider}
import net.minecraft.world.gen.ChunkProviderFlat

/**
 * No description given
 *
 * @author jk-5
 */
class ChunkProviderVoid(val world: World) extends ChunkProviderFlat(world, world.getSeed, false, null) {

  override def loadChunk(x: Int, z: Int): Chunk = {
    this.provideChunk(x, z)
  }

  override def populate(provider: IChunkProvider, x: Int, z: Int){

  }

  override def provideChunk(x: Int, z: Int): Chunk = {
    val ret = new Chunk(this.world, new Array[Block](32768), x, z)
    this.world.getWorldChunkManager.loadBlockGeneratorData(null, x * 16, z * 16, 16, 16)
    ret.generateSkylightMap()
    ret
  }
}
