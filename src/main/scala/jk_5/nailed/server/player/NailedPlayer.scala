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

package jk_5.nailed.server.player

import java.io.{ByteArrayOutputStream, IOException}
import java.util
import java.util.UUID

import com.google.common.base.Charsets
import com.google.common.collect.ImmutableSet
import io.netty.util.CharsetUtil
import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{BaseComponent, ClickEvent, HoverEvent, TextComponent}
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.material.ItemStack
import jk_5.nailed.api.messaging.StandardMessenger
import jk_5.nailed.api.player.{GameMode, Player}
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.{Checks, Location, Potion}
import jk_5.nailed.api.world.World
import jk_5.nailed.server.NailedEventFactory
import jk_5.nailed.server.scoreboard.PlayerScoreboardManager
import jk_5.nailed.server.teleport.Teleporter
import jk_5.nailed.server.utils.ItemStackConverter
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.server.{S02PacketChat, S3FPacketCustomPayload}
import net.minecraft.network.{NetHandlerPlayServer, Packet}
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.world.WorldSettings.GameType
import org.apache.logging.log4j.LogManager

import scala.collection.JavaConverters._
import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class NailedPlayer(private val uuid: UUID, private var name: String) extends Player {

  var entity: EntityPlayerMP = _
  var world: World = _
  var map: Map = _
  private var displayName: String = this.name
  var netHandler: NetHandlerPlayServer = _
  var isOnline: Boolean = false
  var isAllowedToFly: Boolean = false
  val channels = mutable.HashSet[String]()

  override val getScoreboardManager = new PlayerScoreboardManager(this)

  override def getName = this.name
  override def getDisplayName = this.displayName
  override def getUniqueId = this.uuid
  override def hasPermission(permission: String) = true //TODO
  override def sendMessage(message: BaseComponent) = this.netHandler.sendPacket(new S02PacketChat(message))
  override def sendMessage(messages: BaseComponent*) = this.netHandler.sendPacket(new S02PacketChat(messages: _*))
  override def sendMessage(messages: Array[BaseComponent]) = this.netHandler.sendPacket(new S02PacketChat(messages: _*))
  override def getPlayer = this
  override def getLastPlayed: Long = 0
  override def hasPlayedBefore: Boolean = false
  override def getFirstPlayed: Long = 0
  override def isBanned: Boolean = false

  override def teleportTo(world: World){
    Teleporter.teleportPlayer(this, new TeleportOptions(if(world.getConfig != null) {val s = world.getConfig.spawnPoint; s.setWorld(world); s} else new Location(world, 0, 64, 0)))
  }

  def getEntity = this.entity
  def getWorld = this.world
  def getMap = this.map
  def getLocation = new Location(this.world, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)

  def sendPacket(packet: Packet) = if(this.netHandler != null) this.netHandler.sendPacket(packet)

  override def getGameMode: GameMode = getEntity.theItemInWorldManager.getGameType match {
    case GameType.SURVIVAL => GameMode.SURVIVAL
    case GameType.CREATIVE => GameMode.CREATIVE
    case GameType.ADVENTURE => GameMode.ADVENTURE
    case e => throw new IllegalStateException("Player has unknown game mode " + e.getName + " " + e.getID)
  }

  override def setGameMode(gm: GameMode): Unit = getEntity.setGameType(gm match {
    case GameMode.SURVIVAL => GameType.SURVIVAL
    case GameMode.CREATIVE => GameType.CREATIVE
    case GameMode.ADVENTURE => GameType.ADVENTURE
  })

  override def setAllowedToFly(allowed: Boolean){
    this.isAllowedToFly = allowed
    this.getEntity.capabilities.allowFlying = allowed
    this.getEntity.capabilities.isFlying = allowed
    this.getEntity.sendPlayerAbilities()
  }

  override def getInventorySize: Int = this.getEntity.inventory.getSizeInventory
  override def getInventorySlotContent(slot: Int): ItemStack = ItemStackConverter.toNailed(this.getEntity.inventory.getStackInSlot(slot)) //TODO: maybe save inventories in our system instead the vanilla one
  override def setInventorySlot(slot: Int, stack: ItemStack){
    this.getEntity.inventory.setInventorySlotContents(slot, ItemStackConverter.toVanilla(stack))
    this.getEntity.updateCraftingInventory(this.getEntity.inventoryContainer, this.getEntity.inventoryContainer.getInventory)
  }

  override def addToInventory(stack: ItemStack){
    this.getEntity.inventory.addItemStackToInventory(ItemStackConverter.toVanilla(stack))
    this.getEntity.updateCraftingInventory(this.getEntity.inventoryContainer, this.getEntity.inventoryContainer.getInventory)
  }

  override def iterateInventory(p: ItemStack => Unit){
    for(i <- 0 until this.getInventorySize) p(getInventorySlotContent(i))
  }

  override def kick(reason: String){
    this.netHandler.kickPlayerFromServer(reason)
  }

  override def getDescriptionComponent: BaseComponent = {
    val c = new TextComponent(this.getDisplayName)
    c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getUniqueId.toString)))
    c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName + " "))
    c
  }

  override def setHealth(health: Float){
    if(health < 0 || health > getMaxHealth){
      throw new IllegalArgumentException("Health must be between 0 and " + getMaxHealth)
    }

    if(health == 0){
      entity.onDeath(DamageSource.generic)
    }

    entity.setHealth(health)
  }

  override def damage(amount: Float): Unit = entity.attackEntityFrom(DamageSource.generic, amount)
  override def getHealth: Float = Math.min(Math.max(0, getEntity.getHealth), getMaxHealth)
  override def getMaxHealth = getEntity.getMaxHealth

  override def setMaxHealth(health: Float){
    Checks.check(health > 0, "Max health must be greater than 0")

    entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(health)

    if(getHealth > health){
      setHealth(health)
    }
  }

  override def resetMaxHealth() = setMaxHealth(entity.getMaxHealth)
  override def heal() = setHealth(getMaxHealth)
  override def heal(amount: Int){
    Checks.check(amount > 0, "Amount must be greater than 0")
    setHealth(getHealth + amount)
  }

  override def addInstantPotionEffect(effect: Potion) = addInstantPotionEffect(effect, 1)
  override def addInstantPotionEffect(effect: Potion, level: Int){
    Checks.check(effect.isInstant, "Potion is not instant")
    Checks.check(level > 0, "Level should be > 0")
    this.entity.addPotionEffect(new PotionEffect(effect.getId, 1, level - 1))
  }
  override def addPotionEffect(effect: Potion, seconds: Int) = addPotionEffect(effect, seconds, 1)
  override def addPotionEffect(effect: Potion, seconds: Int, level: Int) = addPotionEffect(effect, seconds, level, ambient = false)
  override def addPotionEffect(effect: Potion, seconds: Int, level: Int, ambient: Boolean){
    Checks.check(!effect.isInstant, "Potion is instant")
    Checks.check(level > 0, "Level should be > 0")
    this.entity.addPotionEffect(new PotionEffect(effect.getId, seconds * 20, level - 1, ambient))
  }
  override def addInfinitePotionEffect(effect: Potion) = addPotionEffect(effect, 1000000)
  override def addInfinitePotionEffect(effect: Potion, level: Int) = addPotionEffect(effect, 1000000, level)
  override def addInfinitePotionEffect(effect: Potion, level: Int, ambient: Boolean) = addPotionEffect(effect, 1000000, level, ambient)

  override def clearPotionEffects() = entity.clearActivePotions()
  override def clearPotionEffect(effect: Potion) = entity.removePotionEffect(effect.getId)

  override def loadResourcePack(url: String){
    sendPacket(new S3FPacketCustomPayload("MC|RPack", url.getBytes(Charsets.UTF_8)))
  }

  override def addExperienceLevel(level: Int) = entity.addExperienceLevel(level)
  override def addExperience(level: Int) = entity.addExperience(level)
  override def setExperienceLevel(level: Int) = entity.experienceLevel = level
  override def experienceLevelCap = entity.xpBarCap()
  override def getExperience = entity.experience.toInt
  override def getExperienceLevel = entity.experienceLevel

  override def sendPluginMessage(source: Plugin, channel: String, message: Array[Byte]){
    StandardMessenger.validatePluginMessage(Server.getInstance.getMessenger, source, channel, message)
    if(netHandler == null) return

    if(channels.contains(channel)){
      sendPacket(new S3FPacketCustomPayload(channel, message))
    }
  }

  def sendSupportedChannels(){
    if(netHandler == null) return
    val listening = Server.getInstance.getMessenger.getIncomingChannels

    if(!listening.isEmpty){
      val stream = new ByteArrayOutputStream
      for(channel <- listening){
        try{
          stream.write(channel.getBytes(CharsetUtil.UTF_8))
          stream.write(0.toByte)
        }catch{
          case e: IOException => LogManager.getLogger.error("Could not send Plugin Channel REGISTER to " + getName, e)
        }
      }

      sendPacket(new S3FPacketCustomPayload("REGISTER", stream.toByteArray))
    }
  }

  def addChannel(channel: String){
    if(channels.add(channel)){
      NailedEventFactory.firePlayerRegisterChannelEvent(this, channel)
    }
  }

  def removeChannel(channel: String){
    if(channels.remove(channel)){
      NailedEventFactory.firePlayerUnregisterChannelEvent(this, channel)
    }
  }

  def getListeningPluginChannels: util.Set[String] = ImmutableSet.copyOf(channels.asJava: java.lang.Iterable[String])

  override def toString = s"NailedPlayer{uuid=$uuid,name=$name,isOnline=$isOnline,gameMode=$getGameMode,eid=${getEntity.getEntityId}}"
}
