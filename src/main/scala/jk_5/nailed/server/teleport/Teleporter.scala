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

package jk_5.nailed.server.teleport

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.{Location, TeleportOptions}
import jk_5.nailed.api.world.{Dimension, World}
import jk_5.nailed.server.NailedPlatform
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.{Entity, EntityList, EntityLiving}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.{S07PacketRespawn, S1DPacketEntityEffect, S1FPacketSetExperience}
import net.minecraft.potion.PotionEffect
import net.minecraft.world.WorldServer
import org.apache.commons.lang3.Validate

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object Teleporter {

  def teleportPlayer(player: Player, opt: TeleportOptions): Boolean = {
    Validate.notNull(player, "player")
    Validate.notNull(opt, "opt")

    val options = opt.copy //We don't want to accidently modify the options object passed in, so we copy it.
    val currentWorld = player.getWorld
    if(!TeleportEventFactory.isTeleportAllowed(currentWorld, opt.getDestination.getWorld, player, options)){
      return false
    }
    val location = TeleportEventFactory.alterDestination(currentWorld, opt.getDestination.getWorld, player, options)
    val destinationWorld = location.getWorld

    val entity = player.asInstanceOf[NailedPlayer].getEntity
    teleportEntity(currentWorld, destinationWorld, entity, location, options)
    true
  }

  def teleportEntity(currentWorld: World, destinationWorld: World, ent: Entity, location: Location, options: TeleportOptions): Entity = {
    var entity = ent
    val dimension = destinationWorld.getDimensionId
    val destWorld = destinationWorld.asInstanceOf[NailedWorld].wrapped

    val player: Player = ent match {
      case p: EntityPlayerMP => NailedPlatform.getPlayer(p.getGameProfile.getId)
      case _ => null
    }

    if(player != null && !TeleportEventFactory.isTeleportAllowed(currentWorld, destinationWorld, player, options)){
      return entity
    }
    var mount = entity.ridingEntity
    if(entity.ridingEntity != null){
      entity.mountEntity(null)
      mount = teleportEntity(currentWorld, destinationWorld, mount, location, options)
    }
    val mX = entity.motionX
    val mY = entity.motionY
    val mZ = entity.motionZ
    val changingworlds = entity.worldObj != destWorld
    if(player != null) TeleportEventFactory.onLinkStart(currentWorld, destinationWorld, player, options)
    entity.worldObj.updateEntityWithOptionalForce(entity, false)
    entity match {
      case p: EntityPlayerMP =>
        p.closeScreen()
        if(changingworlds){
          val oldDimension = currentWorld.getDimension
          val newDimension = destinationWorld.getDimension

          p.dimension = dimension
          if(oldDimension != newDimension){
            p.playerNetServerHandler.sendPacket(new S07PacketRespawn(newDimension.getId, destWorld.getDifficulty, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
          }else{
            if(newDimension == Dimension.END){
              p.playerNetServerHandler.sendPacket(new S07PacketRespawn(-1, destWorld.getDifficulty, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
            }else{
              p.playerNetServerHandler.sendPacket(new S07PacketRespawn(1, destWorld.getDifficulty, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
            }
            p.playerNetServerHandler.sendPacket(new S07PacketRespawn(newDimension.getId, destWorld.getDifficulty, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
          }
          p.worldObj.asInstanceOf[WorldServer].getPlayerManager.removePlayer(p)
        }
      case _ =>
    }
    if(changingworlds){
      removeEntityFromWorld(entity.worldObj, entity)
    }
    if(player != null) TeleportEventFactory.onExitWorld(currentWorld, destinationWorld, player, options)

    entity.setLocationAndAngles(location.getX, location.getX, location.getZ, location.getYaw, location.getPitch)
    destWorld.theChunkProviderServer.loadChunk((location.getFloorX >> 4).toInt, (location.getFloorZ >> 4).toInt)
    if(changingworlds){
      if(!entity.isInstanceOf[EntityPlayerMP]){
        val entityNBT = new NBTTagCompound
        entity.isDead = false
        entity.writeToNBTOptional(entityNBT)
        entity.isDead = true
        entity = EntityList.createEntityFromNBT(entityNBT, destWorld)
        if(entity == null){
          return null
        }
        entity.dimension = destWorld.provider.getDimensionId
      }
      destWorld.spawnEntityInWorld(entity)
      entity.setWorld(destWorld)
    }
    entity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
    if(player != null) TeleportEventFactory.onEnterWorld(currentWorld, destinationWorld, player, options)
    destWorld.updateEntityWithOptionalForce(entity, false)
    entity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
    entity match {
      case p: EntityPlayerMP =>
        if(changingworlds){
          p.mcServer.getConfigurationManager.preparePlayer(p, destWorld)
        }
        p.playerNetServerHandler.setPlayerLocation(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
      case _ =>
    }
    destWorld.updateEntityWithOptionalForce(entity, false)
    entity match {
      case p: EntityPlayerMP if changingworlds =>
        if(player != null){
          val pl = player.asInstanceOf[NailedPlayer]
          pl.world = location.getWorld
          pl.map = pl.world.getMap
        }
        p.theItemInWorldManager.setWorld(destWorld)
        p.mcServer.getConfigurationManager.updateTimeAndWeatherForPlayer(p, destWorld)
        p.mcServer.getConfigurationManager.syncPlayerInventory(p)
        for(effect <- p.getActivePotionEffects){
          p.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(p.getEntityId, effect.asInstanceOf[PotionEffect]))
        }
        p.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(p.experience, p.experienceTotal, p.experienceLevel))
      case _ =>
    }
    entity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
    if(player != null) TeleportEventFactory.onEnd(currentWorld, destinationWorld, player, options)
    /*if(options.isMaintainMomentum()){
      entity.motionX = mX;
      entity.motionY = mY;
      entity.motionZ = mZ;
    }*/
    if(mount != null){
      if(entity.isInstanceOf[EntityPlayerMP]){
        destWorld.updateEntityWithOptionalForce(entity, true)
      }
      entity.mountEntity(mount)
    }
    entity match {
      case e: EntityLiving => e.setHealth(e.getMaxHealth)
      case _ =>
    }
    entity
  }

  def removeEntityFromWorld(world: net.minecraft.world.World, entity: Entity) = entity match {
    case p: EntityPlayerMP =>
      p.closeScreen()
      world.playerEntities.remove(p)
      world.updateAllPlayersSleepingFlag()
      val x = entity.chunkCoordX
      val z = entity.chunkCoordZ
      if(entity.addedToChunk && world.getChunkProvider.chunkExists(x, z)){
        val chunk = world.getChunkFromChunkCoords(x, z)
        chunk.removeEntity(entity)
        chunk.setChunkModified()
      }
      world.loadedEntityList.remove(entity)
      world.onEntityRemoved(entity)
    case _ =>
      world.removeEntity(entity)
      world.onEntityRemoved(entity)
  }
}
