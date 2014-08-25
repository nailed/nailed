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

package jk_5.nailed.server.map.gamerule

import jk_5.nailed.api.mappack.gamerule.EditableGameRules

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class DefaultEditableGameRules extends EditableGameRules {

  val rules = mutable.HashMap[String, String]()

  override def apply(key: String): String = this.rules.getOrElse(key, "")
  override def update(key: String, value: String): Unit = this.rules.put(key, value)
  override def ruleExists(key: String) = this.rules.contains(key)
  override def list: Seq[String] = this.rules.keySet.toSeq

  override def toString = s"DefaultEditableGameRules{}"
}
