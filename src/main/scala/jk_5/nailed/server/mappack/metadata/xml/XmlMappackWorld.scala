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

import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.mappack.implementation.DefaultMappackWorldProperties
import jk_5.nailed.api.util.{Checks, Location}
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
    Location(null, x, y, z, yaw, pitch)
  }
}

class XmlMappackWorld(override val name: String, element: Element, parent: MappackWorld = DefaultMappackWorldProperties) extends MappackWorld {
  Checks.notNull(parent, "parent may not be null")

  override val generator = if(element.getChild("generator", element.getNamespace) != null) element.getChild("generator", element.getNamespace).getText else parent.generator
  override val dimension = if(element.getChild("dimension", element.getNamespace) != null) element.getChild("dimension", element.getNamespace).getText match {
    case "nether" => -1
    case "overworld" => 0
    case "end" => 1
    case e => throw new RuntimeException("Unknown world generator type '"  + e + "'")
  } else parent.dimension
  override val spawnPoint = if(element.getChild("spawnpoint", element.getNamespace) != null) XmlMappackWorld.readLocation(element.getChild("spawnpoint", element.getNamespace)) else parent.spawnPoint
  override val gameRules = new ImmutableXmlGameRules(if(element.getChild("gamerules", element.getNamespace) != null) element.getChild("gamerules", element.getNamespace) else new Element("dummy", element.getNamespace), parent.gameRules)
}