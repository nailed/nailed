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

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import net.minecraft.world.gen.ChunkProviderServer

import scala.util.Properties

/**
 * No description given
 *
 * @author jk-5
 */
case class QueuedChunk(x: Int, z: Int, loader: AnvilChunkLoader, world: World, provider: ChunkProviderServer) {

  private[chunkloading] var nbt: NBTTagCompound = _

  override def hashCode() = (x * 31 + z * 29) ^ world.hashCode()

  override def equals(obj: scala.Any): Boolean = obj match {
    case c: QueuedChunk => x == c.x && z == c.z && world == c.world
    case _ => false
  }

  override def toString: String = {
    val result = new StringBuilder
    val NEW_LINE = Properties.lineSeparator

    result.append("QueuedChunk{x=").append(x).append(",z=").append(z).append(",loader=").append(loader).append(",world=").append(world.getWorldInfo.getWorldName).append(",dimension=").append(world.provider.dimensionId).append(",provider=").append(world.provider.getClass().getName()).append('}')
    result.toString()
  }
}
