package jk_5.nailed.worldedit

import com.google.common.base.Preconditions.checkNotNull
import com.sk89q.worldedit.entity.metadata.EntityType
import net.minecraft.entity.{Entity, EntityLiving, IMerchant, INpc, IProjectile}
import net.minecraft.entity.item.{EntityBoat, EntityEnderEye, EntityFallingBlock, EntityItem, EntityItemFrame, EntityMinecart, EntityPainting, EntityTNTPrimed, EntityXPOrb}
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.passive.{EntityAmbientCreature, EntityTameable, IAnimals}
import net.minecraft.entity.player.EntityPlayer

/**
 * No description given
 *
 * @author jk-5
 */
class WorldEditEntityType(val entity: Entity) extends EntityType {
  checkNotNull(entity)

  override def isPlayerDerived = entity.isInstanceOf[EntityPlayer]
  override def isProjectile = entity.isInstanceOf[EntityEnderEye] || entity.isInstanceOf[IProjectile]
  override def isItem = entity.isInstanceOf[EntityItem]
  override def isFallingBlock = entity.isInstanceOf[EntityFallingBlock]
  override def isPainting = entity.isInstanceOf[EntityPainting]
  override def isItemFrame = entity.isInstanceOf[EntityItemFrame]
  override def isBoat = entity.isInstanceOf[EntityBoat]
  override def isMinecart = entity.isInstanceOf[EntityMinecart]
  override def isTNT = entity.isInstanceOf[EntityTNTPrimed]
  override def isExperienceOrb = entity.isInstanceOf[EntityXPOrb]
  override def isLiving = entity.isInstanceOf[EntityLiving]
  override def isAnimal = entity.isInstanceOf[IAnimals]
  override def isAmbient = entity.isInstanceOf[EntityAmbientCreature]
  override def isNPC = entity.isInstanceOf[INpc] || entity.isInstanceOf[IMerchant]
  override def isGolem = entity.isInstanceOf[EntityGolem]
  override def isTamed = entity.isInstanceOf[EntityTameable] && entity.asInstanceOf[EntityTameable].isTamed
  override def isTagged = entity.isInstanceOf[EntityLiving] && entity.asInstanceOf[EntityLiving].hasCustomNameTag
}
