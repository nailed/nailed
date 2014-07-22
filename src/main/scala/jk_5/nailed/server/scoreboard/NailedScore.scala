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
