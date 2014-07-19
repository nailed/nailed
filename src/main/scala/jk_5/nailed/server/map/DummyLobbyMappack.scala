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
