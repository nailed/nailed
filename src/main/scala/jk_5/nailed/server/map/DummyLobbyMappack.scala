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

package jk_5.nailed.server.map

import java.io.File

import io.netty.util.concurrent.Promise
import jk_5.nailed.api.mappack.Mappack

/**
 * No description given
 *
 * @author jk-5
 */
object DummyLobbyMappack extends Mappack {
  override val getId = "nailed:lobby"
  override def getMetadata = DummyLobbyMappackMetadata
  override def prepareWorld(destinationDirectory: File, promise: Promise[Void]){
    promise.setSuccess(null)
  }
}
