package jk_5.nailed.api.world

import jk_5.nailed.api.mappack.MappackWorld
import jk_5.nailed.api.util.Checks

/**
 * No description given
 *
 * @author jk-5
 */
case class WorldContext(name: String, subName: String, config: MappackWorld = null){
  Checks.notNull(name, "name")
  Checks.notNull(subName, "subName")
}
