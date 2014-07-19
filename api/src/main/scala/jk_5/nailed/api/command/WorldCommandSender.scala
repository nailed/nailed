package jk_5.nailed.api.command

import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
trait WorldCommandSender extends CommandSender {

  def getWorld: World
}
