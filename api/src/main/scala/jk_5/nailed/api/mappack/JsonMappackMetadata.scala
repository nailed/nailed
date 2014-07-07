package jk_5.nailed.api.mappack

import com.google.gson.{JsonObject, JsonParseException}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class JsonMappackMetadata(json: JsonObject) extends MappackMetadata {

  override val name = json.get("name").getAsString
  override val worlds: Array[MappackWorld] = json.getAsJsonArray("worlds").map{e =>
    if(e.isJsonObject){
      val obj = e.getAsJsonObject
      new JsonMappackWorld(obj.get("name").getAsString, obj)
    }else if(e.isJsonPrimitive){
      val prim = e.getAsJsonPrimitive
      new JsonMappackWorld(prim.getAsString, new JsonObject)
    }else throw new JsonParseException("Invalid object type in worlds array: " + e.toString)
  }.toArray
}
