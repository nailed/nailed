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

package jk_5.nailed.plugins.qrmap

import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.world.storage.MapData

/**
 * No description given
 *
 * @author jk-5
 */
object QRMapCommand extends Command("qrmap") {

  override def execute(sender: CommandSender, args: Array[String]){
    sender match {
      case p: NailedPlayer =>
        val ent = p.getEntity
        val is = new ItemStack(Items.filled_map, 1, ent.worldObj.getUniqueDataId("map"))
        val name = "map_" + is.getItemDamage
        val data = new MapData(name)
        ent.worldObj.setItemData(name, data)
        data.scale = 0
        data.xCenter = 0
        data.zCenter = 0
        data.dimension = 0
        data.colors = MapRenderer.renderString("test")
        data.markDirty()
        ent.inventory.addItemStackToInventory(is)
    }
  }
}
