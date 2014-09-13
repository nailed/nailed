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

import java.io.{File, FileReader}

import com.google.gson.{JsonObject, JsonParser, JsonPrimitive}
import jk_5.nailed.api.map.stat.StatConfig
import jk_5.nailed.api.mappack._
import jk_5.nailed.api.mappack.tutorial.Tutorial

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object JsonMappackMetadata {

  def fromFile(file: File): JsonMappackMetadata = {
    val reader = new FileReader(file)
    val obj = new JsonParser().parse(reader).getAsJsonObject
    reader.close()
    new JsonMappackMetadata(obj)
  }
}

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

  override val stats: Array[StatConfig] = if(!json.has("stats")) new Array[StatConfig](0) else json.getAsJsonArray("stats").map{
    case e: JsonObject => new JsonStatConfig(e)
    case e => throw new MappackConfigurationException("Invalid object type in stats array: " + e.toString)
  }.toArray

  override val tutorial: Tutorial = if(json.has("tutorial")) new JsonTutorial(json.getAsJsonObject("tutorial")) else null
  override val gameType = if(json.has("gametype")) json.get("gametype").getAsString else null

  for(world <- worlds){
    if(worlds.count(_.name == world.name) != 1){
      throw new MappackConfigurationException("There are more than 1 worlds with the same name ('" + world.name + "')")
    }
  }
}
