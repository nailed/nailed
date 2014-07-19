package jk_5.nailed.server.teleport

import jk_5.nailed.api.player.Player
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World
import jk_5.nailed.server.NailedServer
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.world.NailedWorld
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.{Entity, EntityList}
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
    /*if(!TeleportEventFactory.isTeleportAllowed(currentWorld, destinationWorld, entity, options)){
      return false
    }*/
    var mount = entity.ridingEntity
    if(entity.ridingEntity != null){
      entity.mountEntity(null)
      mount = teleportEntity(currentWorld, destinationWorld, mount, location, options)
    }
    val mX = entity.motionX
    val mY = entity.motionY
    val mZ = entity.motionZ
    val changingworlds = entity.worldObj != destWorld
    //TeleportEventFactory.onLinkStart(currentWorld, destinationWorld, entity, options)
    entity.worldObj.updateEntityWithOptionalForce(entity, false)
    entity match {
      case p: EntityPlayerMP =>
        p.closeScreen()
        if(changingworlds){
          val oldType = currentWorld.getType
          val newType = destinationWorld.getType

          p.dimension = dimension
          if(oldType != newType){
            p.playerNetServerHandler.sendPacket(new S07PacketRespawn(newType, destWorld.difficultySetting, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
          }else{
            if(newType == 1){
              p.playerNetServerHandler.sendPacket(new S07PacketRespawn(-1, destWorld.difficultySetting, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
            }else{
              p.playerNetServerHandler.sendPacket(new S07PacketRespawn(1, destWorld.difficultySetting, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
            }
            p.playerNetServerHandler.sendPacket(new S07PacketRespawn(newType, destWorld.difficultySetting, destWorld.getWorldInfo.getTerrainType, p.theItemInWorldManager.getGameType))
          }
          p.worldObj.asInstanceOf[WorldServer].getPlayerManager.removePlayer(p)
        }
      case _ =>
    }
    if(changingworlds){
      removeEntityFromWorld(entity.worldObj, entity)
    }
    //TeleportEventFactory.onExitWorld(currentMap, destMap, entity, options);

    entity.setLocationAndAngles(location.getX, location.getX, location.getZ, location.getYaw, location.getPitch)
    destWorld.theChunkProviderServer.loadChunk(location.getBlockX >> 4, location.getBlockZ >> 4)
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
        entity.dimension = destWorld.provider.dimensionId
      }
      destWorld.spawnEntityInWorld(entity)
      entity.setWorld(destWorld)
    }
    entity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
    //TeleportEventFactory.onEnterWorld(currentMap, destMap, entity, options)
    destWorld.updateEntityWithOptionalForce(entity, false)
    entity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
    entity match {
      case p: EntityPlayerMP =>
        if(changingworlds){
          p.mcServer.getConfigurationManager.func_72375_a(p, destWorld)
        }
        p.playerNetServerHandler.setPlayerLocation(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
      case _ =>
    }
    destWorld.updateEntityWithOptionalForce(entity, false)
    entity match {
      case p: EntityPlayerMP if changingworlds =>
        NailedServer.getPlayer(p.getGameProfile.getId) match {
          case Some(pl) => pl.asInstanceOf[NailedPlayer].world = location.getWorld
          case None =>
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
    //TeleportEventFactory.onLinkEnd(currentMap, destMap, entity, options);
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
        chunk.isModified = true
      }
      world.loadedEntityList.remove(entity)
      world.onEntityRemoved(entity)
    case _ =>
      world.removeEntity(entity)
      world.onEntityRemoved(entity)
  }
}
