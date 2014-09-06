package jk_5.nailed.worldedit

import com.google.common.base.Preconditions.checkNotNull
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.entity.metadata.EntityType
import com.sk89q.worldedit.entity.{BaseEntity, Entity}
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.world.NullWorld
import net.minecraft.entity.EntityList
import net.minecraft.nbt.NBTTagCompound

import scala.ref.WeakReference

/**
 * No description given
 *
 * @author jk-5
 */
class WorldEditEntity(entity: net.minecraft.entity.Entity) extends Entity {
  checkNotNull(entity)
  val entityRef = new WeakReference[net.minecraft.entity.Entity](entity)

  def getState: BaseEntity = entityRef.get match {
    case Some(ent) =>
      val id = EntityList.getEntityString(ent)
      if(id != null){
        val tag = new NBTTagCompound
        ent.writeToNBT(tag)
        new BaseEntity(id, NBTConverter.fromNative(tag))
      }else{
        null
      }
    case None => null
  }

  def getLocation: Location = entityRef.get match {
    case Some(ent) =>
      val position = new Vector(entity.posX, entity.posY, entity.posZ)
      new Location(WorldEditWorld.getWorld(entity.worldObj), position, entity.rotationYaw, entity.rotationPitch)
    case None => new Location(NullWorld.getInstance)
  }

  def getExtent: Extent = entityRef.get match {
    case Some(ent) => WorldEditWorld.getWorld(ent.worldObj)
    case None => NullWorld.getInstance()
  }

  def remove: Boolean = entityRef.get match {
    case Some(ent) =>
      ent.setDead()
      true
    case None => true
  }

  def getFacet[T](cls: Class[_ <: T]): T = entityRef.get match {
    case Some(ent) if classOf[EntityType].isAssignableFrom(cls) => new WorldEditEntityType(ent).asInstanceOf[T]
    case _ => null.asInstanceOf[T]
  }
}
