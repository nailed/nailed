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

package jk_5.nailed.plugins.directorymappackloader

import java.io.File

import io.netty.util.concurrent.Promise
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.mappack.filesystem.DirectoryMount
import jk_5.nailed.api.mappack.metadata.MappackMetadata
import org.apache.commons.io.FileUtils

/**
 * No description given
 *
 * @author jk-5
 */
class DirectoryMappack(private val dir: File, override val getMetadata: MappackMetadata) extends Mappack {

  override val getId = dir.getName
  override def prepareWorld(destinationDirectory: File, promise: Promise[Void]){
    val original = new File(dir, "worlds")
    destinationDirectory.mkdir()
    FileUtils.copyDirectory(original, destinationDirectory)
    promise.setSuccess(null)
  }
  override def getMappackMount = new DirectoryMount(new File(dir, "scripts"))

  override def toString = s"DirectoryMappack{id=$getId,metadata=$getMetadata,dir=$dir}"
}
