package jk_5.nailed.plugins.nmm

import java.io.{File, FileOutputStream}

import io.netty.util.concurrent.Promise
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.mappack.filesystem.DirectoryMount
import jk_5.nailed.api.mappack.metadata.MappackMetadata
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata
import jk_5.nailed.server.utils.ZipUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

class NmmMappack(path: String) extends Mappack {

  private var dir: File = _
  private var metadata: MappackMetadata = _

  override def getId = path

  override def getMetadata = metadata

  override def prepareWorld(destinationDirectory: File, promise: Promise[Void]){
    destinationDirectory.mkdir()
    val httpClient = HttpClientBuilder.create().build()
    try{
      val mappack = path.split("/", 2)(1)
      val request = new HttpGet("http://nmm.jk-5.tk/" + path + "/versions.json")
      val response = httpClient.execute(request)
      val list = NmmPlugin.instance.gson.fromJson(EntityUtils.toString(response.getEntity, "UTF-8"), classOf[MappackInfo])

      val request2 = new HttpGet("http://nmm.jk-5.tk/" + path + "/" + mappack + "-" + list.latest + ".zip")
      val response2 = httpClient.execute(request2).getEntity
      if(response2 != null){
        val mappackZip = new File(destinationDirectory, "mappack.zip")
        val is = response2.getContent
        val os = new FileOutputStream(mappackZip)
        IOUtils.copy(is, os)
        os.close()
        ZipUtils.extract(mappackZip, destinationDirectory)
        mappackZip.delete()
        dir = destinationDirectory
        val dataDir = new File(destinationDirectory, ".data")
        dataDir.mkdir()
        val metadataLocation = new File(dataDir, "game.xml")
        new File(destinationDirectory, "game.xml").renameTo(metadataLocation)
        new File(destinationDirectory, "scripts").renameTo(new File(dataDir, "scripts"))
        val worldsDir = new File(destinationDirectory, "worlds")
        worldsDir.listFiles().foreach { f =>
          f.renameTo(new File(destinationDirectory, f.getName))
        }
        worldsDir.delete()
        metadata = XmlMappackMetadata.fromFile(metadataLocation)
        promise.setSuccess(null)
      }else{
        promise.setFailure(new RuntimeException("Got an empty response while downloading mappack " + path + " from nmm.jk-5.tk"))
      }
    }catch{
      case e: Exception =>
        promise.setFailure(new RuntimeException("Was not able to download mappack " + path + " from nmm.jk-5.tk", e))
    }
  }

  override def getMappackMount = new DirectoryMount(new File(new File(dir, ".data"), "scripts"))
}
