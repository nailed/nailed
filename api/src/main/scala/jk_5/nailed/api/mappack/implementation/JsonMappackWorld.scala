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
import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.ImmutableJsonGameRules
import jk_5.nailed.api.util.{Checks, Location}
import jk_5.nailed.api.world.Difficulty

/**
 * No description given
 *
 * @author jk-5
 */
class JsonMappackWorld(override val name: String, json: JsonObject, parent: MappackWorld = DefaultMappackWorldProperties) extends MappackWorld {
  Checks.notNull(parent, "parent may not be null")

  override val generator = if(json.has("generator")) json.get("generator").getAsString else parent.generator
  override val dimension = if(json.has("dimension")) json.get("dimension").getAsString  match {
    case "nether" => -1
    case "overworld" => 0
    case "end" => 1
    case e => throw new RuntimeException("Unknown world generator type '"  + e + "'")
  } else parent.dimension
  override val spawnPoint = if(json.has("spawnpoint")) Location.read(json.getAsJsonObject("spawnpoint")) else parent.spawnPoint
  override val gameRules = new ImmutableJsonGameRules(if(json.has("gamerules")) json.getAsJsonObject("gamerules") else new JsonObject, parent.gameRules)
  override val resourcepack = if(json.has("resourcepack")) json.get("resourcepack").getAsString else parent.resourcepack
  override val difficulty = if(json.has("difficulty")) Difficulty.byName(json.get("difficulty").getAsString) else parent.difficulty
}
