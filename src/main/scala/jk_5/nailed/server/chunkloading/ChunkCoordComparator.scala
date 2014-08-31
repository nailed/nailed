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

import java.util.Comparator

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.ChunkCoordIntPair

/**
 * No description given
 *
 * @author jk-5
 */
class ChunkCoordComparator(private val x: Int, private val z: Int) extends Comparator[ChunkCoordIntPair] {

  def this(player: EntityPlayerMP) = this(player.posX.toInt >> 4, player.posZ.toInt >> 4)

  override def compare(a: ChunkCoordIntPair, b: ChunkCoordIntPair): Int = {
    if(a == b) return 0

    // Subtract current position to set center point
    val ax = a.chunkXPos - this.x
    val az = a.chunkZPos - this.z
    val bx = b.chunkXPos - this.x
    val bz = b.chunkZPos - this.z
    val result = ((ax - bx) * (ax + bx)) + ((az - bz) * (az + bz))

    if(result != 0) return result
    if(ax < 0){
      if(bx < 0) bz - az else -1
    }else{
      if(bx < 0) 1 else az - bz
    }
  }
}
