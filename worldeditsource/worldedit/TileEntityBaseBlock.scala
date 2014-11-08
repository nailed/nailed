package jk_5.nailed.worldedit

import com.sk89q.worldedit.blocks.{BaseBlock, TileEntityBlock}

/**
 * No description given
 *
 * @author jk-5
 */
class TileEntityBaseBlock(typ: Int, data: Int) extends BaseBlock(typ, data) with TileEntityBlock
