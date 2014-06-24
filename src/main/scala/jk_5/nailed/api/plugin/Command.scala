package jk_5.nailed.api.plugin

import jk_5.nailed.api.command.CommandSender
import org.apache.commons.lang3.Validate

/**
 * A command that can be executed by a {@link CommandSender}
 *
 * @author jk-5
 */
abstract case class Command(private val name: String, private val aliases: String*) {
  Validate.notNull(name, "name")

  private var owner: Plugin = _

  private[plugin] def setOwner(plugin: Plugin) = this.owner = plugin
  def getOwner = this.owner
  def getName = this.name
  def getAliases = this.aliases
  def execute(sender: CommandSender, args: Array[String])
}
