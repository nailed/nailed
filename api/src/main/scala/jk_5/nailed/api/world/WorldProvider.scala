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

package jk_5.nailed.api.world

/**
 * No description given
 *
 * @author jk-5
 */
trait WorldProvider {

  def setId(id: Int)
  def getId: Int

  /**
   * What kind of type is this world?
   *  -1 for nether
   *   0 for overworld
   *   1 for end
   *
   * Defaults to 0 (overworld)
   *
   * @return the world type
   */
  def getTypeId: Int

  //TODO: this is temporary
  def getType: String
  def getOptions: String
}
