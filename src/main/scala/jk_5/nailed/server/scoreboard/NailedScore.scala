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

package jk_5.nailed.server.scoreboard

import jk_5.nailed.api.scoreboard.Score
import jk_5.nailed.api.util.Checks
import net.minecraft.network.play.server.S3CPacketUpdateScore

/**
 * No description given
 *
 * @author jk-5
 */
class NailedScore(val owner: NailedObjective, val name: String) extends Score {
  Checks.notNull(owner, "owner may not be null")
  Checks.notNull(name, "name may not be null")
  Checks.check(name.length() <= 16, "name may not be longer than 16")

  var value: Int = 0
  override def setValue(value: Int){
    this.value = value
    update()
  }
  override def addValue(value: Int) = this.setValue(this.value + value)

  override def update(){
    val p = new S3CPacketUpdateScore
    p.field_149329_a = this.name
    p.field_149327_b = this.owner.id
    p.field_149328_c = this.value
    p.field_149326_d = 0
    owner.manager.sendPacket(p)
  }
}
