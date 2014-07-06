package jk_5.nailed.api.mappack

import java.io.File

import io.netty.util.concurrent.Promise

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
}
