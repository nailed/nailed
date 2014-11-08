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

import java.io.File

import jk_5.nailed.api.mappack._
import jk_5.nailed.api.mappack.metadata._
import jk_5.nailed.api.mappack.metadata.impl.DefaultMappackAuthor
import org.jdom2.input.SAXBuilder
import org.jdom2.{Element, Namespace}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object XmlMappackMetadata {

  final val builder = new SAXBuilder()

  def fromFile(file: File): XmlMappackMetadata = {
    val doc = builder.build(file)
    val e = doc.getRootElement
    if(e.getName == "game"){
      new XmlMappackMetadata(e, e.getNamespace)
    }else{
      throw new MappackConfigurationException("game.xml does not contain the root game element")
    }
  }
}

class XmlMappackMetadata(element: Element, ns: Namespace) extends MappackMetadata {
  if(element.getChild("name", ns) == null) throw new MappackConfigurationException("Missing required <name> element")
  if(element.getChild("version", ns) == null) throw new MappackConfigurationException("Missing required <version> element")
  if(element.getChild("authors", ns) == null) throw new MappackConfigurationException("Missing required <authors> element")

  override val name = element.getChild("name", ns).getText
  override val version = element.getChild("version", ns).getText

  override val authors: Array[MappackAuthor] = element.getChild("authors", ns).getChildren.map{ e =>
    if(e.getName != "author") throw new MappackConfigurationException("Invalid element in authors list: " + e.getName)
    if(e.getChild("name", ns) == null) throw new MappackConfigurationException("Missing required element <name> in <author> element")
    if(e.getChild("role", ns) == null) throw new MappackConfigurationException("Missing required element <role> in <author> element")
    new DefaultMappackAuthor(e.getChild("name", ns).getText, e.getChild("role", ns).getText)
  }.toArray

  override val tutorial = if(element.getChild("tutorial", ns) != null) new XmlTutorial(element.getChild("tutorial", ns)) else null
  override val teams: Array[MappackTeam] = if(element.getChild("teams", ns) == null) new Array[MappackTeam](0) else element.getChild("teams", ns).getChildren.map(e => new XmlMappackTeam(e)).toArray
  override val worlds: Array[MappackWorld] = element.getChild("worlds", ns).getChildren.map { e =>
    if(e.getChild("name", ns) == null) throw new MappackConfigurationException("Missing required element <name> in <world> element")
    new XmlMappackWorld(e.getChild("name", ns).getText, e)
  }.toArray
  override val gameType = if(element.getChild("gametype", ns) != null) element.getChild("gametype", ns).getAttributeValue("name", ns) else null
  override val stats: Array[StatConfig] = if(element.getChild("stats", ns) != null) element.getChild("stats", ns).getChildren.map{ e =>
    if(e.getName != "stat") throw new MappackConfigurationException("Invalid element in stats list: " + e.getName)
    val name = e.getAttributes.find(_.getName == "name")
    if(name.isEmpty || name.get.getValue.length == 0) throw new MappackConfigurationException("Missing required attribute 'name' in stat element")
    new XmlStatConfig(name.get.getValue, e)
  }.toArray else new Array[StatConfig](0)
}
