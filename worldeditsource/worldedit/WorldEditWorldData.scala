package jk_5.nailed.worldedit

import com.sk89q.worldedit.world.registry.LegacyWorldData

/**
 * No description given
 *
 * @author jk-5
 */
object WorldEditWorldData extends LegacyWorldData {
  override def getBiomeRegistry = WorldEditBiomeRegistry
}
