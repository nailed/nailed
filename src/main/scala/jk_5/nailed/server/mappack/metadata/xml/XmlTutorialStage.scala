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

import jk_5.nailed.api.mappack.MappackConfigurationException
import jk_5.nailed.api.mappack.tutorial.TutorialStage
import org.jdom2.Element

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class XmlTutorialStage(element: Element) extends TutorialStage {
  if(element.getChild("title", element.getNamespace) == null) throw new MappackConfigurationException("Tutorial stage does not have a title")

  override val title = element.getChild("title", element.getNamespace).getText
  override val messages = if(element.getChild("messages", element.getNamespace) == null) new Array[String](0) else element.getChild("messages", element.getNamespace).getChildren.map(_.getText).toArray
  override val teleport = if(element.getChild("teleport", element.getNamespace) == null) null else XmlMappackWorld.readLocation(element.getChild("teleport", element.getNamespace))
}
