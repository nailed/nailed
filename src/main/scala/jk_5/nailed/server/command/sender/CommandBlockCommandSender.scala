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

package jk_5.nailed.server.command.sender

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.command.CommandSender

/**
 * No description given
 *
 * @author jk-5
 */
class CommandBlockCommandSender extends CommandSender {

  /**
   * Get the unique name of this command sender.
   *
   * @return the senders username
   */
  override def getName: String = ???

  /**
   * Checks if this user has the specified permission node.
   *
   * @param permission the node to check
   * @return whether they have this node
   */
  override def hasPermission(permission: String): Boolean = ???

  /**
   * Send a message to this sender.
   *
   * @param message the message to send
   */
  override def sendMessage(message: BaseComponent): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: BaseComponent*): Unit = ???

  /**
   * Send a message to this sender.
   *
   * @param messages the message to send
   */
  override def sendMessage(messages: Array[BaseComponent]): Unit = ???
}
