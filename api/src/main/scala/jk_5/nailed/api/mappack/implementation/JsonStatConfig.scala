package jk_5.nailed.api.mappack.implementation

import java.util

import com.google.gson.JsonObject
import jk_5.nailed.api.map.stat.StatConfig
import jk_5.nailed.api.util.Checks

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class JsonStatConfig(json: JsonObject) extends StatConfig {
  Checks.check(json.has("track"), "Stat should have a name")

  override val name = json.get("name").getAsString
  override val track = if(json.has("name")) json.get("name").getAsString else null
  override val attributes = if(json.has("attributes")){
    val ret = new util.HashMap[String, String]()
    for(c <- json.getAsJsonObject("attributes").entrySet()) ret.put(c.getKey, c.getValue.getAsString)
    ret
  }else new util.HashMap[String, String]()
}
