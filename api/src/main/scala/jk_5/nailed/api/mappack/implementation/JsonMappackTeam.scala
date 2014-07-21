package jk_5.nailed.api.mappack.implementation

import com.google.gson.JsonObject
import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.mappack.{MappackConfigurationException, MappackTeam}

/**
 * No description given
 *
 * @author jk-5
 */
class JsonMappackTeam(private val json: JsonObject) extends MappackTeam {
  if(!json.has("id")) throw new MappackConfigurationException("Invalid team. Team doesn't have an id object")
  if(!json.has("name")) throw new MappackConfigurationException("Invalid team. Team doesn't have a name object")
  if(!json.has("color")) throw new MappackConfigurationException("Invalid team. Team doesn't have a color")

  override val id = json.get("id").getAsString
  override val name = json.get("name").getAsString
  override val color = ChatColor.getByName(json.get("color").getAsString)

  if(color == null) throw new MappackConfigurationException("Invalid team color '" + json.get("color").getAsString + "'. Color does not exist")
}
