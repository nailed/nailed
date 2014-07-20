package jk_5.nailed.plugins.directorymappackloader

import java.io.{File, FileReader}

import com.google.gson.JsonParser
import io.netty.util.concurrent.Promise
import jk_5.nailed.api.mappack.{JsonMappackMetadata, Mappack}
import org.apache.commons.io.FileUtils

/**
 * No description given
 *
 * @author jk-5
 */
class DirectoryMappack(private val dir: File) extends Mappack {

  override val getId = dir.getName
  override val getMetadata = new JsonMappackMetadata({
    val reader = new FileReader(new File(dir, "mappack.json"))
    val obj = new JsonParser().parse(reader).getAsJsonObject
    reader.close()
    obj
  })
  override def prepareWorld(destinationDirectory: File, promise: Promise[Void]){
    val original = new File(dir, "worlds")
    destinationDirectory.mkdir()
    FileUtils.copyDirectory(original, destinationDirectory)
    promise.setSuccess(null)
  }

  override def toString = s"DirectoryMappack{id=$getId,metadata=$getMetadata,dir=$dir}"
}
