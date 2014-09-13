package jk_5.nailed.server.mappack.metadata.xml

import java.util

import jk_5.nailed.api.map.stat.StatConfig
import org.jdom2.Element

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class XmlStatConfig(val name: String, element: Element) extends StatConfig {
  override val track = element.getAttributes.find(_.getName == "track") match {
    case Some(t) => t.getValue
    case None => null
  }
  override val attributes = {
    val ret = new util.HashMap[String, String]()
    for(c <- element.getChildren) ret.put(c.getName, c.getText)
    ret
  }
}
