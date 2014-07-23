package jk_5.nailed.api.mappack.implementation

import com.google.gson.JsonObject
import jk_5.nailed.api.mappack.MappackConfigurationException
import jk_5.nailed.api.mappack.tutorial.{Tutorial, TutorialStage}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class JsonTutorial(json: JsonObject) extends Tutorial {
  override val stages: Array[TutorialStage] = if(!json.has("stages")) new Array[TutorialStage](0) else json.getAsJsonArray("stages").map {
    case o: JsonObject => new JsonTutorialStage(o)
    case e => throw new MappackConfigurationException("Invalid json element in tutorial stages array: " + e.toString)
  }.toArray
}
