package jk_5.nailed.server.event

import jk_5.eventbus.Event
import jk_5.eventbus.Event.Cancelable
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

/**
 * No description given
 *
 * @author jk-5
 */
@Cancelable case class PlayerRightClickItemEvent(player: EntityPlayer, stack: ItemStack) extends Event
