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
  val defaultProperties = new JsonMappackWorld(null, if(json.has("defaultProperties")) json.getAsJsonObject("defaultProperties") else new JsonObject, DefaultMappackWorldProperties)
  override val worlds: Array[MappackWorld] = json.getAsJsonArray("worlds").map{e =>
    if(e.isJsonObject){
      val obj = e.getAsJsonObject
      new JsonMappackWorld(obj.get("name").getAsString, obj, defaultProperties)
    }else if(e.isJsonPrimitive){
      val prim = e.getAsJsonPrimitive
      new JsonMappackWorld(prim.getAsString, new JsonObject, defaultProperties)
    }else throw new JsonParseException("Invalid object type in worlds array: " + e.toString)
  }.toArray

  for(world <- worlds){
    if(worlds.count(_.name == world.name) != 1){
      throw new RuntimeException("There are more than 1 worlds with the same name ('" + world.name + "')") //TODO
    }
  }
}
