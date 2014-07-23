package jk_5.nailed.api.mappack.implementation

import com.google.gson.{JsonObject, JsonPrimitive}
import jk_5.nailed.api.mappack.MappackConfigurationException
import jk_5.nailed.api.mappack.tutorial.TutorialStage
import jk_5.nailed.api.util.Location

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class JsonTutorialStage(json: JsonObject) extends TutorialStage {
  if(!json.has("title")) throw new MappackConfigurationException("Tutorial stage does not have a title")

  override val title = json.get("title").getAsString
  override val messages = if(!json.has("messages")) new Array[String](0) else json.getAsJsonArray("messages").map {
    case s: JsonPrimitive => s.getAsString
    case s => throw new MappackConfigurationException("Invalid json element in tutorial messages array: " + s.toString)
  }.toArray
  override val teleport = if(!json.has("teleport")) null else Location.read(json.getAsJsonObject("teleport"))
}
