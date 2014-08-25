/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

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
