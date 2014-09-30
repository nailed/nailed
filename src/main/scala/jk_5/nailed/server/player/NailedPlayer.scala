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

import com.google.common.collect.ImmutableSet
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil
import jk_5.nailed.api.chat.{BaseComponent, ClickEvent, HoverEvent, TextComponent}
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.math.{EulerDirection, Vector3d, Vector3f}
import jk_5.nailed.api.messaging.StandardMessenger
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.PluginIdentifier
import jk_5.nailed.api.potion.Potion
import jk_5.nailed.api.util.{Checks, Location, TeleportOptions}
import jk_5.nailed.api.world.World
import jk_5.nailed.api.{GameMode, potion}
import jk_5.nailed.server.scoreboard.PlayerScoreboardManager
import jk_5.nailed.server.teleport.Teleporter
import jk_5.nailed.server.{NailedEventFactory, NailedPlatform}
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
  override def sendMessage(messages: BaseComponent*) = this.netHandler.sendPacket(new S02PacketChat(messages: _*))

  override def heal(amount: Double){
    val newAmount = getHealth + amount
    setHealth(Math.min(getMaxHealth, newAmount))
  }
  override def getMaxHealth = getEntity.getMaxHealth
  override def resetMaxHealth() = setMaxHealth(entity.getMaxHealth)
  override def setMaxHealth(maxHealth: Double){
    Checks.positive(maxHealth, "Max health must be greater than 0")
    entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth)
    if(getHealth > maxHealth){
      setHealth(maxHealth)
    }
  }

  override def getBurnDuration = this.entity.fire
  override def setBurnDuration(ticks: Int) = this.entity.fire = ticks
  override def isBurning = this.entity.fire != 0

  override def setExperience(experience: Double) = entity.experience = experience.toFloat
  override def getExperience = entity.experience.toInt
  override def getLevel = entity.experienceLevel
  override def setLevel(level: Int) = entity.experienceLevel = level

  override def damage(amount: Double): Unit = setHealth(getHealth - amount)
  override def getHealth = Math.min(Math.max(0, getEntity.getHealth), getMaxHealth)
  override def setHealth(health: Double){
    Checks.positiveOrZero(health, "health")
    Checks.smallerThanOrEqual(health, getMaxHealth, "health")
    if(health == 0) entity.onDeath(DamageSource.generic)
    entity.setHealth(health.toFloat)
  }

  override def getPosition = this.getLocation
  override def setPosition(position: Vector3d) = ???
  override def setVectorRotation(rotation: Vector3f) = ???
  override def getVectorRotation = ???
  override def getRotation = ???
  override def setRotation(rotation: EulerDirection) = ???
  override def setVelocity(velocity: Vector3f) = ???
  override def getVelocity = ???
  override def setSaturation(saturation: Double) = ???
  override def setHunger(hunger: Double) = ???
  override def getHunger = ???
  override def getSaturation = ???

  override def teleportTo(world: World){
    Teleporter.teleportPlayer(this, new TeleportOptions(if(world.getConfig != null) Location.builder().copy(world.getConfig.spawnPoint()).setWorld(world).build() else new Location(world, 0, 64, 0)))
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
    case GameType.SPECTATOR => GameMode.SPECTATOR
    case e => throw new IllegalStateException("Player has unknown game mode " + e.getName + " " + e.getID)
  }

  override def setGameMode(gm: GameMode): Unit = getEntity.setGameType(gm match {
    case GameMode.SURVIVAL => GameType.SURVIVAL
    case GameMode.CREATIVE => GameType.CREATIVE
    case GameMode.ADVENTURE => GameType.ADVENTURE
    case GameMode.SPECTATOR => GameType.SPECTATOR
  })

  override def setAllowedToFly(allowed: Boolean){
    this.isAllowedToFly = allowed
    this.getEntity.capabilities.allowFlying = allowed
    this.getEntity.capabilities.isFlying = allowed
    this.getEntity.sendPlayerAbilities()
  }

  /*override def getInventorySize: Int = this.getEntity.inventory.getSizeInventory
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
  }*/

  override def kick(reason: String){
    this.netHandler.kickPlayerFromServer(reason)
  }

  override def getDescriptionComponent: BaseComponent = {
    val c = new TextComponent(this.getDisplayName)
    c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getUniqueId.toString)))
    c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName + " "))
    c
  }

  override def addPotionEffect(effect: potion.PotionEffect){
    val vanillaEffect = new PotionEffect(effect.getPotion.getId, effect.getDuration, effect.getLevel - 1, effect.isAmbient, effect.isShowParticles)
    this.entity.addPotionEffect(vanillaEffect)
  }

  override def removePotionEffect(potion: Potion){
    this.entity.removePotionEffect(potion.getId)
  }

  override def clearAllEffects(){
    this.entity.clearActivePotions()
  }

  override def getActiveEffects = {
    val newCollection = ImmutableSet.builder[potion.PotionEffect]()
    for(effect <- this.entity.getActivePotionEffects.asInstanceOf[java.util.Collection[PotionEffect]]){
      val builder = potion.PotionEffect.builder()
      val pot = Potion.byId(effect.getPotionID)
      if(pot != null){
        builder.setPotion(pot)
        builder.setAmbient(effect.getIsAmbient)
        builder.setShowParticles(effect.func_180154_f)
        builder.setLevel(effect.getAmplifier + 1)
        builder.setDuration(effect.getDuration)
        newCollection.add(builder.build())
      }
    }
    newCollection.build()
  }


  override def loadResourcePack(url: String, hash: String){
    this.entity.func_175397_a(url, hash)
  }

  override def sendPluginMessage(source: PluginIdentifier, channel: String, message: Array[Byte]){
    StandardMessenger.validatePluginMessage(NailedPlatform.getMessenger, source, channel, message)
    if(netHandler == null) return

    if(channels.contains(channel)){
      sendPacket(new S3FPacketCustomPayload(channel, Unpooled.copiedBuffer(message)))
    }
  }

  def sendSupportedChannels(){
    if(netHandler == null) return
    val listening = NailedPlatform.getMessenger.getIncomingChannels

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

      sendPacket(new S3FPacketCustomPayload("REGISTER", Unpooled.copiedBuffer(stream.toByteArray)))
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
