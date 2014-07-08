package jk_5.nailed.api.mappack

import com.google.gson.JsonObject
import jk_5.nailed.api.util.Location

/**
 * No description given
 *
 * @author jk-5
 */
class JsonMappackWorld(override val name: String, json: JsonObject) extends MappackWorld {
  override val generator = if(json.has("generator")) json.get("generator").getAsString else "void"
  override val dimension = (if(json.has("dimension")) json.get("dimension").getAsString else "overworld") match {
    case "nether" => -1
    case "overworld" => 0
    case "end" => 1
    case e => throw new RuntimeException("Unknown world generator type '"  + e + "'")
  }
  override val spawnPoint = Location.read(if(json.has("spawnpoint")) json.getAsJsonObject("spawnpoint") else new JsonObject)
}
