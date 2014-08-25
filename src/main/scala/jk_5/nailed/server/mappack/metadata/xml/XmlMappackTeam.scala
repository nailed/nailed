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

import jk_5.nailed.api.chat.ChatColor
import jk_5.nailed.api.mappack.{MappackConfigurationException, MappackTeam}
import org.jdom2.Element

/**
 * No description given
 *
 * @author jk-5
 */
class XmlMappackTeam(element: Element) extends MappackTeam {
  if(element.getChild("id", element.getNamespace) == null) throw new MappackConfigurationException("Invalid team. Team doesn't have an id element")
  if(element.getChild("name", element.getNamespace) == null) throw new MappackConfigurationException("Invalid team. Team doesn't have a name element")
  if(element.getChild("color", element.getNamespace) == null) throw new MappackConfigurationException("Invalid team. Team doesn't have a color")

  override val id = element.getChild("id", element.getNamespace).getText
  override val name = element.getChild("name", element.getNamespace).getText
  override val color = ChatColor.getByName(element.getChild("color", element.getNamespace).getText)

  if(color == null) throw new MappackConfigurationException("Invalid team color '" + element.getChild("color", element.getNamespace).getText + "'. Color does not exist")
}
