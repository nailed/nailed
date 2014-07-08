package jk_5.nailed.api.world

import jk_5.nailed.api.mappack.MappackWorld
import org.apache.commons.lang3.Validate

/**
 * No description given
 *
 * @author jk-5
 */
case class WorldContext(name: String, subName: String, config: MappackWorld = null){
  Validate.notNull(name, "name")
  Validate.notNull(subName, "subName")
}
