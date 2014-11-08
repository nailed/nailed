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

package jk_5.nailed.server.mappack.metadata.xml

import jk_5.nailed.api.gamerule.{GameRule, GameRules}
import jk_5.nailed.api.mappack.metadata.MappackWorld
import jk_5.nailed.api.mappack.metadata.impl.DefaultMappackWorld
import jk_5.nailed.api.util.{Checks, Location}
import jk_5.nailed.api.world.{Difficulty, Dimension}
import org.jdom2.Element

/**
 * No description given
 *
 * @author jk-5
 */
object XmlMappackWorld {

  def readLocation(e: Element): Location = {
    val x = if(e.getAttribute("x") != null) e.getAttribute("x").getDoubleValue else 0
    val y = if(e.getAttribute("y") != null) e.getAttribute("y").getDoubleValue else 64
    val z = if(e.getAttribute("z") != null) e.getAttribute("z").getDoubleValue else 0
    val yaw = if(e.getAttribute("yaw") != null) e.getAttribute("yaw").getFloatValue else 0
    val pitch = if(e.getAttribute("pitch") != null) e.getAttribute("pitch").getFloatValue else 0
    new Location(x, y, z, yaw, pitch)
  }
}

class XmlMappackWorld(override val name: String, element: Element, parent: MappackWorld = DefaultMappackWorld.INSTANCE) extends MappackWorld {
  Checks.notNull(parent, "parent may not be null")

  override val generator = if(element.getChild("generator", element.getNamespace) != null) element.getChild("generator", element.getNamespace).getText else parent.generator
  override val dimension = if(element.getChild("dimension", element.getNamespace) != null) element.getChild("dimension", element.getNamespace).getText match {
    case "nether" => Dimension.NETHER
    case "overworld" => Dimension.OVERWORLD
    case "end" => Dimension.END
    case e => throw new RuntimeException("Unknown world generator type '"  + e + "'")
  } else parent.dimension
  override val spawnPoint = if(element.getChild("spawnpoint", element.getNamespace) != null) XmlMappackWorld.readLocation(element.getChild("spawnpoint", element.getNamespace)) else parent.spawnPoint
  override val gameRules = new ImmutableXmlGameRules(if(element.getChild("gamerules", element.getNamespace) != null) element.getChild("gamerules", element.getNamespace) else new Element("dummy", element.getNamespace), parent.gameRules.asInstanceOf[GameRules[GameRule[_]]])
  override val resourcePackUrl = if(element.getChild("resourcepack", element.getNamespace) != null) element.getChild("resourcepack", element.getNamespace).getText else parent.resourcePackUrl()
  override val difficulty = if(element.getChild("difficulty", element.getNamespace) != null) Difficulty.byName(element.getChild("difficulty", element.getNamespace).getText) else parent.difficulty
  override val disableFood = if(element.getChild("disableFood", element.getNamespace) != null) element.getChild("disableFood", element.getNamespace).getText.equalsIgnoreCase("true") else parent.disableFood
  override val disableDamage = if(element.getChild("disableDamage", element.getNamespace) != null) element.getChild("disableDamage", element.getNamespace).getText.equalsIgnoreCase("true") else parent.disableDamage
  override val disableBlockBreaking = if(element.getChild("disableBlockBreaking", element.getNamespace) != null) element.getChild("disableBlockBreaking", element.getNamespace).getText.equalsIgnoreCase("true") else parent.disableBlockBreaking
  override val disableBlockPlacement = if(element.getChild("disableBlockPlacement", element.getNamespace) != null) element.getChild("disableBlockPlacement", element.getNamespace).getText.equalsIgnoreCase("true") else parent.disableBlockPlacement
  override val isDefault = element.getAttributeValue("default", element.getNamespace, "false") == "true"
}
