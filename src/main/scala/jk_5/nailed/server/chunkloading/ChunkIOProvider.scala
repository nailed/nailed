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

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.chunk.Chunk
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class ChunkIOProvider extends AsynchronousExecutor.CallBackProvider[QueuedChunk, Chunk, Runnable, RuntimeException]{
  private val threadNumber = new AtomicInteger(1)
  private val logger = LogManager.getLogger

  //Async phase
  override def callStage1(queuedChunk: QueuedChunk): Chunk = {
    val loader = queuedChunk.loader

    var data: Array[AnyRef] = null
    try{
      data = loader.loadChunk__Async(queuedChunk.world, queuedChunk.x, queuedChunk.z)
    }catch{
      case e: IOException => logger.warn("IOException while reading chunk from disk", e)
    }

    if(data != null){
      queuedChunk.nbt = data(1).asInstanceOf[NBTTagCompound]
      data(0).asInstanceOf[Chunk]
    }else null
  }

  //Sync phase
  override def callStage2(queuedChunk: QueuedChunk, chunk: Chunk){
    if(chunk == null){
      //If the async loading failed, just do it synchronously (which may generate a new chunk)
      queuedChunk.provider.originalLoadChunk(queuedChunk.x, queuedChunk.z)
      return
    }

    queuedChunk.loader.loadEntities(queuedChunk.world, queuedChunk.nbt.getCompoundTag("Level"), chunk)
    chunk.lastSaveTime = queuedChunk.provider.worldObj.getTotalWorldTime
    queuedChunk.provider.loadedChunkHashMap.add(ChunkCoordIntPair.chunkXZ2Int(queuedChunk.x, queuedChunk.z), chunk)
    queuedChunk.provider.loadedChunks.asInstanceOf[java.util.List[Chunk]].add(chunk)
    chunk.onChunkLoad()

    if(queuedChunk.provider.currentChunkProvider != null){
      queuedChunk.provider.currentChunkProvider.recreateStructures(queuedChunk.x, queuedChunk.z)
    }

    chunk.populateChunk(queuedChunk.provider, queuedChunk.provider, queuedChunk.x, queuedChunk.z)
  }

  //Final phase
  override def callStage3(queuedChunk: QueuedChunk, chunk: Chunk, callback: Runnable){
    callback.run()
  }

  override def newThread(r: Runnable): Thread = {
    val thread = new Thread(r, "Chunk I/O Executor Thread-" + threadNumber.getAndIncrement)
    thread.setDaemon(true)
    thread
  }
}
