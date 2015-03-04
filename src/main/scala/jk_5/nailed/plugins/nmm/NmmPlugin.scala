package jk_5.nailed.plugins.nmm

import java.io.File

import com.google.gson.Gson
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent
import jk_5.nailed.api.plugin.Plugin
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager

object NmmPlugin {
  var instance: NmmPlugin = _
}

@Plugin(id = "Nailed|NMM", name = "Nailed Mappack Manager", version = "1.0.0")
class NmmPlugin {
  NmmPlugin.instance = this

  val cache = new File("nmm-cache")
  val gson = new Gson
  val logger = LogManager.getLogger
  if(!cache.exists()) cache.mkdir()

  @EventHandler
  def registerMappacks(event: RegisterMappacksEvent){
    val httpClient = HttpClientBuilder.create().build()
    try{
      val request = new HttpGet("http://nmm.jk-5.tk/mappacks.json")
      val response = httpClient.execute(request)
      val list = gson.fromJson(EntityUtils.toString(response.getEntity, "UTF-8"), classOf[MappacksList])
      list.mappacks.foreach { e =>
        event.registerMappack(new NmmMappack(e))
      }
      logger.info("Registered " + list.mappacks.size + " nmm mappacks")
    }catch{
      case e: Exception => logger.warn("Was not able to fetch mappacks from nmm.jk-5.tk", e)
    }
  }
}
