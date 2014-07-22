package jk_5.nailed.api.mappack.implementation

import com.google.gson.JsonObject
import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.gamerule.ImmutableJsonGameRules
import jk_5.nailed.api.util.{Checks, Location}

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
}