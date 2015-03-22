package jk_5.nailed.server.world.voidgen;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;

public class ChunkProviderVoid extends ChunkProviderFlat {

    private final World world;

    public ChunkProviderVoid(World world) {
        super(world, world.getSeed(), false, null);
        this.world = world;
    }

    //FIXME
      /*override def loadChunk(x: Int, z: Int): Chunk = {
        this.provideChunk(x, z)
      }*/

    @Override
    public void populate(IChunkProvider provider, int x, int z) {

    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Chunk ret = new Chunk(this.world, x, z);
        this.world.getWorldChunkManager().loadBlockGeneratorData(null, x * 16, z * 16, 16, 16);
        ret.generateSkylightMap();
        return ret;
    }
}
