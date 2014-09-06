package jk_5.nailed.worldedit

import java.lang.reflect.Constructor

import com.google.common.base.Preconditions.checkNotNull
import com.sk89q.worldedit.Vector
import net.minecraft.nbt.{NBTTagCompound, NBTTagInt}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * Utility methods for setting tile entities in the world.
 *
 * @author jk-5
 */
object TileEntityUtils {

  private def updateForSet(tag: NBTTagCompound, position: Vector): NBTTagCompound = {
    checkNotNull(tag)
    checkNotNull(position)
    tag.setTag("x", new NBTTagInt(position.getBlockX))
    tag.setTag("y", new NBTTagInt(position.getBlockY))
    tag.setTag("z", new NBTTagInt(position.getBlockZ))
    tag
  }

  private[worldedit] def setTileEntity(world: World, position: Vector, clazz: Class[_ <: TileEntity], tag: NBTTagCompound){
    checkNotNull(world)
    checkNotNull(position)
    checkNotNull(clazz)
    val tileEntity = constructTileEntity(world, position, clazz)
    if(tileEntity == null) return
    if(tag != null){
      updateForSet(tag, position)
      tileEntity.readFromNBT(tag)
    }
    world.setTileEntity(position.getBlockX, position.getBlockY, position.getBlockZ, tileEntity)
  }

  private[worldedit] def setTileEntity(world: World, position: Vector, tag: NBTTagCompound) {
    if(tag != null){
      updateForSet(tag, position)
      val tileEntity = TileEntity.createAndLoadEntity(tag)
      if(tileEntity != null){
        world.setTileEntity(position.getBlockX, position.getBlockY, position.getBlockZ, tileEntity)
      }
    }
  }

  private[worldedit] def constructTileEntity(world: World, position: Vector, clazz: Class[_ <: TileEntity]): TileEntity = {
    var baseConstructor: Constructor[_ <: TileEntity] = null
    try{
      baseConstructor = clazz.getConstructor()
    }catch{
      case e: Throwable => return null
    }
    var genericTE: TileEntity = null
    try{
      genericTE = baseConstructor.newInstance()
    }catch {
      case e: Throwable => return null
    }
    genericTE
  }
}
