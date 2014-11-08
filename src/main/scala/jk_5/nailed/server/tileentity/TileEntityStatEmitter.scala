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

package jk_5.nailed.server.tileentity

import jk_5.nailed.api
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.map.stat.{Stat, StatBlock, StatListener}
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.command.server.CommandBlockLogic
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.server.gui.IUpdatePlayerListBox
import net.minecraft.tileentity.TileEntityCommandBlock
import net.minecraft.util.{IChatComponent, Vec3}
import net.minecraft.world.World

/**
 * No description given
 *
 * @author jk-5
 */
class TileEntityStatEmitter extends TileEntityCommandBlock with StatBlock with StatListener with IUpdatePlayerListBox {

  var content = ""
  var tick = 0
  var subscribed: Stat = _
  var map: Map = _
  var register = false
  var response = "Enter a stat id in the box above"

  override def update(){
    if(register && getWorld != null && map != null){
      subscribed = map.getStatManager.getStat(content)
      if(subscribed != null){
        subscribed.addListener(TileEntityStatEmitter.this)
        response = "Stat registered. Stat emitter ready to emit"
      }else{
        response = "Could not register. Stat does not exist"
      }
      register = false
    }
  }

  override def setWorldObj(world: World){
    super.setWorldObj(world)
    map = NailedDimensionManager.getWorld(world.provider.getDimensionId).asInstanceOf[api.world.World].getMap
  }

  //BlockCommandBlock is hardcoded to rely on CommandBlockLogic, and i don't want to change that
  //Because of that, we create a CommandBlockLogic object that intercepts all those calls for our own purpose
  val commandBlockLogic = new CommandBlockLogic {

    override def trigger(worldIn: World){}
    override def getSuccessCount = strength

    override def setCommand(data: String){
      content = data
      if(subscribed != null) subscribed.removeListener(TileEntityStatEmitter.this)
      if(map != null){
        subscribed = map.getStatManager.getStat(content)
        if(subscribed != null){
          subscribed.addListener(TileEntityStatEmitter.this)
          response = "Stat registered. Stat emitter ready to emit"
        }else{
          response = "Could not register. Stat does not exist"
        }
      }else register = true
    }

    //Called when a update should be send to the client
    override def func_145756_e(){
      getWorld.markBlockForUpdate(TileEntityStatEmitter.this.pos)
    }

    override def canCommandSenderUseCommand(level: Int, command: String): Boolean = false
    override def getName = "StatEmitter"

    override def getEntityWorld: World = getWorld
    override def getPosition = TileEntityStatEmitter.this.pos
    override def getPositionVector = new Vec3(getPosition.getX + 0.5, getPosition.getY + 0.5, getPosition.getZ + 0.5)
    override def getCommandSenderEntity = null
    override def addChatMessage(message: IChatComponent){}
  }
  override def getCommandBlockLogic = this.commandBlockLogic

  private var strength = 0

  override def setSignalStrength(strength: Int){
    this.strength = strength
    worldObj.updateComparatorOutputLevel(getPos, Blocks.command_block)
  }

  override def writeToNBT(tag: NBTTagCompound){
    super.writeToNBT(tag)
    tag.setString("Content", this.content)
  }

  override def readFromNBT(tag: NBTTagCompound){
    super.readFromNBT(tag)
    this.commandBlockLogic.setCommand(tag.getString("Content"))
  }

  override def getDescriptionPacket: Packet = {
    val tag = new NBTTagCompound
    super.writeToNBT(tag)
    tag.setString("Command", content)
    tag.setInteger("SuccessCount", 0)
    tag.setString("CustomName", "Stat Emitter")
    tag.setString("LastOutput", s"""{"text": "$response"}""")
    tag.setBoolean("TrackOutput", true)
    new S35PacketUpdateTileEntity(this.getPos, 2, tag)
  }

  def scheduleBlockUpdate() = this.commandBlockLogic.func_145756_e()

  override def onEnable(){
    setSignalStrength(15)
  }

  override def onDisable(){
    setSignalStrength(0)
  }
}
