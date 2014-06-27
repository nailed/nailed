package jk_5.nailed.server.world.void

import java.util
import java.util.Random

import net.minecraft.init.Blocks
import net.minecraft.util.ChunkCoordinates
import net.minecraft.world.biome.WorldChunkManager
import net.minecraft.world.{ChunkPosition, World}

/**
 * No description given
 *
 * @author jk-5
 */
class WorldChunkManagerVoid(val world: World) extends WorldChunkManager(world) {

  override def findBiomePosition(x: Int, z: Int, range: Int, biomes: util.List[_], random: Random): ChunkPosition = {
    var ret = super.findBiomePosition(x, z, range, biomes, random)
    if(x == 0 && z == 0 && !world.getWorldInfo.isInitialized){
      if(ret == null){
        ret = new ChunkPosition(0, 0, 0)
      }
      val spawn = new ChunkCoordinates(0, 63, 0)
      if(world.isAirBlock(spawn.posX, spawn.posY, spawn.posZ)){
        world.setBlock(spawn.posX, spawn.posY, spawn.posZ, Blocks.bedrock)
      }
    }
    ret
  }
}
