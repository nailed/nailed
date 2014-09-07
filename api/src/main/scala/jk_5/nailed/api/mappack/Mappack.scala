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

package jk_5.nailed.api.mappack

import java.io.File

import io.netty.util.concurrent.Promise
import jk_5.nailed.api.map.filesystem.IMount

/**
 * No description given
 *
 * @author jk-5
 */
trait Mappack {

  /**
   * @return An unique name for this mappack
   */
  def getId: String

  /**
   * @return The metadata for this mappack
   */
  def getMetadata: MappackMetadata

  /**
   * This method should prepare the game world at the given location asynchronously, and finish the promise
   *
   * Important: You HAVE to call .setSuccess(null) or .setFailure(throwable) on the promise, or the loading will hang
   *
   * @param destinationDirectory The location where the game world should be prepared
   * @param promise              The callback to call when the map is set up.
   */
  def prepareWorld(destinationDirectory: File, promise: Promise[Void])

  def getMappackMount: IMount
}
