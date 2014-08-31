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

package jk_5.nailed.server.chunkloading

import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import net.minecraft.world.gen.ChunkProviderServer

/**
 * No description given
 *
 * @author jk-5
 */
object ChunkIOExecutor {
  private[chunkloading] final val BASE_THREADS = 1
  private[chunkloading] final val PLAYERS_PER_THREAD = 50

  private final val executor = new AsynchronousExecutor[QueuedChunk, Chunk, Runnable, RuntimeException](new ChunkIOProvider, BASE_THREADS)

  def syncChunkLoad(world: World, loader: AnvilChunkLoader, provider: ChunkProviderServer, x: Int, z: Int): Chunk = {
    executor.getSkipQueue(new QueuedChunk(x, z, loader, world, provider))
  }

  def queueChunkLoad(world: World, loader: AnvilChunkLoader, provider: ChunkProviderServer, x: Int, z: Int, runnable: Runnable){
    executor.add(new QueuedChunk(x, z, loader, world, provider), runnable)
  }

  // Abuses the fact that hashCode and equals for QueuedChunk only use world and coords
  def dropQueuedChunkLoad(world: World, x: Int, z: Int, runnable: Runnable){
    executor.drop(new QueuedChunk(x, z, null, world, null), runnable)
  }

  def adjustPoolSize(players: Int){
    val size = Math.max(BASE_THREADS, Math.ceil(players / PLAYERS_PER_THREAD).toInt)
    executor.setActiveThreads(size)
  }

  def tick(){
    executor.finishActive()
  }
}
