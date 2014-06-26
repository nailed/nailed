package jk_5.nailed.internalplugin

import java.io.File

import jk_5.nailed.api.plugin.PluginDescription
import jk_5.nailed.server.tweaker.NailedVersion

/**
 * No description given
 *
 * @author jk-5
 */
object InternalPluginDescription extends PluginDescription {
  override def getFile = new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.toString)
  override def getName = "Nailed Internal"
  override def getMain = "jk_5.nailed.internalplugin.NailedInternalPlugin"
  override def getVersion = NailedVersion.full
  override def getAuthor = "jk-5"
  override def getDescription = ""
}
