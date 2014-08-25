/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

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
