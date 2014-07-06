package jk_5.nailed.internalplugin.mappack

import java.io.File

import io.netty.util.concurrent.Promise
import jk_5.nailed.api.mappack.{Mappack, MappackMetadata}

/**
 * No description given
 *
 * @author jk-5
 */
object LobbyMappack extends Mappack {

  override def getId = "lobby"
  override def getMetadata: MappackMetadata = null

  override def prepareWorld(destinationDirectory: File, promise: Promise[Void]){
    promise.setSuccess(null)
  }

  override def toString = s"LobbyMappack{id=lobby,metadata=$getMetadata}"
}
