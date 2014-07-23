package jk_5.nailed.api.mappack.implementation

import com.google.gson.{JsonObject, JsonPrimitive}
import jk_5.nailed.api.mappack._
import jk_5.nailed.api.mappack.tutorial.Tutorial

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

  override val worlds: Array[MappackWorld] = json.getAsJsonArray("worlds").map {
    case e: JsonObject => new JsonMappackWorld(e.get("name").getAsString, e, defaultProperties)
    case e: JsonPrimitive => new JsonMappackWorld(e.getAsString, new JsonObject, defaultProperties)
    case e => throw new MappackConfigurationException("Invalid object type in worlds array: " + e.toString)
  }.toArray

  override val teams: Array[MappackTeam] = if(!json.has("teams")) new Array[MappackTeam](0) else json.getAsJsonArray("teams").map{
    case e: JsonObject => new JsonMappackTeam(e)
    case e => throw new MappackConfigurationException("Invalid object type in teams array: " + e.toString)
  }.toArray

  override def tutorial: Tutorial = if(json.has("tutorial")) new JsonTutorial(json.getAsJsonObject("tutorial")) else null

  for(world <- worlds){
    if(worlds.count(_.name == world.name) != 1){
      throw new MappackConfigurationException("There are more than 1 worlds with the same name ('" + world.name + "')")
    }
  }
}
