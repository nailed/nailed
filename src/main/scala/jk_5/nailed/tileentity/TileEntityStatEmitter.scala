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

package jk_5.nailed.tileentity

import jk_5.nailed.api.stat.StatBlock
import net.minecraft.command.server.CommandBlockLogic
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntityCommandBlock
import net.minecraft.util.{ChunkCoordinates, IChatComponent}
import net.minecraft.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class TileEntityStatEmitter extends TileEntityCommandBlock with StatBlock {

  var content = ""
  var tick = 0

  override def updateEntity(){
    if(tick == 20){
      if(strength == 0) this.setSignalStrength(15) else this.setSignalStrength(0)
      tick = 0
    }else tick += 1
  }

  //BlockCommandBlock is hardcoded to rely on CommandBlockLogic, and i don't want to change that
  //Because of that, we create a CommandBlockLogic object that intercepts all those calls for our own purpose
  val commandBlockLogic = new CommandBlockLogic {

    //Called when the block receives a redstone signal
    override def func_145755_a(world: World){}
    override def func_145760_g() = strength

    //Called when a update shoudl be send to the client
    override def func_145756_e(){
      getWorldObj.markBlockForUpdate(TileEntityStatEmitter.this.xCoord, TileEntityStatEmitter.this.yCoord, TileEntityStatEmitter.this.zCoord)
    }

    override def canCommandSenderUseCommand(level: Int, command: String): Boolean = false
    override def getCommandSenderName = "StatEmitter"

    override def getEntityWorld: World = getWorldObj
    override def getPlayerCoordinates = new ChunkCoordinates(TileEntityStatEmitter.this.xCoord, TileEntityStatEmitter.this.yCoord, TileEntityStatEmitter.this.zCoord)

    override def addChatMessage(message: IChatComponent){}
  }
  override def func_145993_a() = this.commandBlockLogic

  private var strength = 0

  override def setSignalStrength(strength: Int){
    this.strength = strength
    worldObj.func_147453_f(xCoord, yCoord, zCoord, Blocks.command_block)
  }

  override def writeToNBT(tag: NBTTagCompound){
    super.writeToNBT(tag)
    tag.setString("Content", this.content)
  }

  override def readFromNBT(tag: NBTTagCompound){
    super.readFromNBT(tag)
    this.content = tag.getString("Content")
  }

  override def getDescriptionPacket: Packet = {
    val tag = new NBTTagCompound
    super.writeToNBT(tag)
    tag.setString("Command", content)
    tag.setInteger("SuccessCount", 0)
    tag.setString("CustomName", "Stat Emitter")
    tag.setString("LastOutput", """{"text": "Enter a stat id in the box above"}""")
    tag.setBoolean("TrackOutput", true)
    new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 2, tag)
  }

  def scheduleBlockUpdate() = this.commandBlockLogic.func_145756_e()
}
