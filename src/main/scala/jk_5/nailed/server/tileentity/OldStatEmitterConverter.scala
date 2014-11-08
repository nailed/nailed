package jk_5.nailed.server.tileentity

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
class OldStatEmitterConverter extends TileEntity {

  val logger = LogManager.getLogger

  override def readFromNBT(tag: NBTTagCompound){
    super.readFromNBT(tag)
    logger.info(tag.toString)
  }

  override def writeToNBT(tag: NBTTagCompound){
    super.writeToNBT(tag)
  }
}
