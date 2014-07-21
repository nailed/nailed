package jk_5.nailed.api.mappack

import com.google.gson.{JsonObject, JsonParseException}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class JsonMappackMetadata(json: JsonObject) extends MappackMetadata {
  if(!json.has("name")) throw new MappackConfigurationException("Mappack does not have a name")
  if(!json.has("version")) throw new MappackConfigurationException("Mappack does not have a version")
  if(!json.has("authors")) throw new MappackConfigurationException("Mappack does not have an authors list")

  override val name = json.get("name").getAsString
  override val version = json.get("version").getAsString
  override val authors: Array[MappackAuthor] = json.getAsJsonObject("authors").entrySet().map{ e =>
    val name = e.getKey
    e.getValue match {
      case element if element.isJsonPrimitive => new DefaultMappackAuthor(name, element.getAsString)
      case element => throw new MappackConfigurationException("Invalid json element in authors list: " + element.toString)
    }
  }.toArray

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
      throw new MappackConfigurationException("There are more than 1 worlds with the same name ('" + world.name + "')") //TODO
    }
  }
}
