package jk_5.nailed.server.player

import java.util.UUID

import jk_5.nailed.api.chat.BaseComponent
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.material.ItemStack
import jk_5.nailed.api.player.{GameMode, Player}
import jk_5.nailed.api.teleport.TeleportOptions
import jk_5.nailed.api.util.Location
import jk_5.nailed.api.world.World
import jk_5.nailed.server.scoreboard.PlayerScoreboardManager
import jk_5.nailed.server.teleport.Teleporter
import jk_5.nailed.server.utils.ItemStackConverter
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.{NetHandlerPlayServer, Packet}
import net.minecraft.world.WorldSettings.GameType

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

  override def setGameMode(gm: GameMode): Unit = getEntity.theItemInWorldManager.setGameType(gm match {
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
    this.getEntity.sendContainerAndContentsToPlayer(this.getEntity.inventoryContainer, this.getEntity.inventoryContainer.getInventory)

  }

  override def addToInventory(stack: ItemStack){
    this.getEntity.inventory.addItemStackToInventory(ItemStackConverter.toVanilla(stack))
    this.getEntity.sendContainerAndContentsToPlayer(this.getEntity.inventoryContainer, this.getEntity.inventoryContainer.getInventory)
  }

  override def iterateInventory(p: ItemStack => Unit){
    for(i <- 0 until this.getInventorySize) p(getInventorySlotContent(i))
  }

  override def toString = s"NailedPlayer{uuid=$uuid,name=$name,isOnline=$isOnline,gameMode=$getGameMode}"
}
