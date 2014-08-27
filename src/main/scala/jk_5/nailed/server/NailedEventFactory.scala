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

package jk_5.nailed.server

import java.util

import jk_5.eventbus.Event
import jk_5.nailed.api
import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.event._
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.material.Material
import jk_5.nailed.api.player.{GameMode, Player}
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.command.sender.ConsoleCommandSender
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.tileentity.TileEntityStatEmitter
import jk_5.nailed.server.utils.ItemStackConverter._
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.block.Block
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemStack, ItemSword}
import net.minecraft.network.play.server.{S05PacketSpawnPosition, S07PacketRespawn, S1FPacketSetExperience, S23PacketBlockChange}
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.management.ItemInWorldManager
import net.minecraft.world.WorldSettings.GameType
import net.minecraft.world.{World, WorldServer}
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedEventFactory {

  object DummyInternalListenerPlugin extends Plugin

  var serverCommandSender: ConsoleCommandSender = _

  private val preTickEvent = new ServerPreTickEvent
  private val postTickEvent = new ServerPostTickEvent

  private val logger = LogManager.getLogger

  def fireEvent[T <: Event](event: T): T = NailedServer.getPluginManager.callEvent(event)

  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, NailedServer)

  def firePreWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPreTickEvent(NailedServer.getWorld(world.provider.dimensionId)))
  }

  def firePostWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPostTickEvent(NailedServer.getWorld(world.provider.dimensionId)))
  }

  def firePreServerTick(server: MinecraftServer) = fireEvent(preTickEvent)
  def firePostServerTick(server: MinecraftServer) = fireEvent(postTickEvent)

  def fireServerStartBeforeConfig(server: DedicatedServer){
    NailedServer.preLoad(server)
    this.serverCommandSender = new ConsoleCommandSender(server)
  }

  def fireServerStarted(server: DedicatedServer){
    NailedServer.load(server)
  }

  def fireStartBeforeWorldLoad(server: DedicatedServer){

  }

  def fireCommand(sender: ICommandSender, input: String): Int = {
    val wrapped = sender match {
      case p: EntityPlayerMP => NailedServer.getPlayer(p.getGameProfile.getId).orNull
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return -1
    if(NailedServer.getPluginManager.dispatchCommand(wrapped, input, null)) 1 else -1
  }

  def fireTabCompletion(sender: ICommandSender, input: String): util.List[String] = {
    val wrapped = sender match {
      case p: EntityPlayerMP => NailedServer.getPlayer(p.getGameProfile.getId).orNull
      /*case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      case r: RConConsoleSource => new RConCommandSender(r)*/
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return null
    val ret = mutable.ListBuffer[String]()
    if(NailedServer.getPluginManager.dispatchCommand(wrapped, input, ret)) ret else null
  }

  def fireWorldLoad(world: World){

  }

  def fireWorldUnload(world: World){

  }

  def fireEntityInPortal(entity: Entity){

  }

  def firePlayerJoined(playerEntity: EntityPlayerMP){
    val player = NailedServer.getOrCreatePlayer(playerEntity).asInstanceOf[NailedPlayer]
    player.entity = playerEntity
    player.isOnline = true
    player.world = NailedServer.getWorld(playerEntity.dimension)
    player.map = player.world.getMap.orNull
    player.netHandler = playerEntity.playerNetServerHandler
    player.world.onPlayerJoined(player)
    player.world.getMap.foreach(_.onPlayerJoined(player))
    player.getScoreboardManager.onJoinedServer()
    val e = this.fireEvent(new PlayerJoinServerEvent(player))
    NailedServer.broadcastMessage(e.joinMessage)
  }

  def firePlayerLeft(playerEntity: EntityPlayerMP){
    val player = NailedServer.getPlayerFromEntity(playerEntity).asInstanceOf[NailedPlayer]
    player.isOnline = false
    val e = this.fireEvent(new PlayerLeaveServerEvent(player))
    player.getScoreboardManager.onLeftServer()
    player.world.onPlayerLeft(player)
    player.world.getMap.foreach(_.onPlayerLeft(player))
    player.entity = null
    player.world = null
    player.map = null
    player.netHandler = null
    NailedServer.broadcastMessage(e.leaveMessage)
  }

  def firePlayerChat(playerEntity: EntityPlayerMP, message: String): String = {
    val player = NailedServer.getPlayerFromEntity(playerEntity)
    val e = this.fireEvent(new PlayerChatEvent(player, message))
    if(e.isCanceled) null else e.message
  }

  def fireOnRightClick(playerEntity: EntityPlayer, world: World, is: ItemStack, x: Int, y: Int, z: Int, side: Int, bX: Float, bY: Float, bZ: Float): Boolean = {
    val player = NailedServer.getPlayerFromEntity(playerEntity.asInstanceOf[EntityPlayerMP]).asInstanceOf[NailedPlayer]
    var xC = x
    var yC = y
    var zC = z
    side match {
      case 0 => yC -= 1
      case 1 => yC += 1
      case 2 => zC -= 1
      case 3 => zC += 1
      case 4 => xC -= 1
      case 5 => xC += 1
    }
    val canceled = fireEvent(new BlockPlaceEvent(xC, yC, zC, NailedDimensionManager.getWorld(world.provider.dimensionId), player)).isCanceled
    val ret = if(canceled){
      //Send the slot content to the client because the client decreases the stack size by 1 when it places a block
      player.getEntity.sendContainerAndContentsToPlayer(player.getEntity.inventoryContainer, player.getEntity.inventoryContainer.getInventory)
      true
    }else false

    if(ret) return true

    //TODO: allow to do this in an event handler
    if(is.getTagCompound != null && is.getTagCompound.getBoolean("IsStatemitter")){
      if(player.getGameMode != GameMode.CREATIVE){
        player.sendMessage(new ComponentBuilder("You must be in creative mode to use Stat Emitters!").color(ChatColor.RED).create())
        player.getEntity.sendContainerAndContentsToPlayer(player.getEntity.inventoryContainer, player.getEntity.inventoryContainer.getInventory)
        return true
      }
      world.setBlock(xC, yC, zC, Blocks.command_block, 8, 3)
      if(is.getTagCompound.hasKey("Content")){
        world.getTileEntity(xC, yC, zC).asInstanceOf[TileEntityStatEmitter].content = is.getTagCompound.getString("Content")
      }
      return true
    }
    ret
  }

  def fireOnBlockBroken(world: World, gameType: GameType, playerEntity: EntityPlayerMP, x: Int, y: Int, z: Int): Boolean = {
    var preCancel = false
    if(gameType.isAdventure && !playerEntity.isCurrentToolAdventureModeExempt(x, y, z)){
      preCancel = true
    }else if(gameType.isCreative && playerEntity.getHeldItem != null && playerEntity.getHeldItem.getItem.isInstanceOf[ItemSword]){
      preCancel = true
    }

    // Tell client the block is gone immediately then process events
    if(world.getTileEntity(x, y, z) == null){
      val packet = new S23PacketBlockChange(x, y, z, world)
      packet.field_148883_d = Blocks.air
      packet.field_148884_e = 0
      playerEntity.playerNetServerHandler.sendPacket(packet)
    }

    //Post the block break event
    val player = Server.getInstance.getPlayer(playerEntity.getGameProfile.getId).get
    val block = world.getBlock(x, y, z)
    val meta = world.getBlockMetadata(x, y, z)
    val event = new BlockBreakEvent(x, y, z, NailedDimensionManager.getWorld(world.provider.dimensionId), Material.getMaterial(Block.getIdFromBlock(block)), meta.toByte, player)
    event.setCanceled(preCancel)
    fireEvent(event)

    if(event.isCanceled){
      //Let the client know the block still exists
      playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world))
      //Also update the TileEntity
      val tile = world.getTileEntity(x, y, z)
      if(tile != null){
        val desc = tile.getDescriptionPacket
        if(desc != null){
          playerEntity.playerNetServerHandler.sendPacket(desc)
        }
      }
      true
    }else false
  }

  def firePlayerDropStack(player: EntityPlayerMP, fullStack: Boolean): Boolean = {
    //Return false to cancel
    !fireEvent(new PlayerThrowItemEvent(NailedServer.getPlayerFromEntity(player), player.getCurrentEquippedItem)).isCanceled
  }

  def firePlayerLeftWorld(player: Player, world: api.world.World) = fireEvent(new PlayerLeaveWorldEvent(player, world))
  def firePlayerJoinWorld(player: Player, world: api.world.World) = fireEvent(new PlayerJoinWorldEvent(player, world))
  def firePlayerLeftMap(player: Player, world: Map) = fireEvent(new PlayerLeaveMapEvent(player, world))
  def firePlayerJoinMap(player: Player, world: Map) = fireEvent(new PlayerJoinMapEvent(player, world))

  def fireOnItemRightClick(player: EntityPlayer, world: World, stack: ItemStack): Boolean = {
    fireEvent(new PlayerRightClickItemEvent(NailedServer.getPlayerFromEntity(player.asInstanceOf[EntityPlayerMP]), stack)).isCanceled
  }

  def onPlayerRespawn(ent: EntityPlayerMP){
    val server = MinecraftServer.getServer
    val player = NailedServer.getPlayerFromEntity(ent).asInstanceOf[NailedPlayer]
    val destWorld = NailedDimensionManager.getWorld(ent.dimension)
    val currentWorld = NailedDimensionManager.getWorld(ent.dimension)
    val destMap = destWorld.getMap
    val currentMap = currentWorld.getMap

    currentWorld.wrapped.getEntityTracker.removePlayerFromTrackers(ent)
    currentWorld.wrapped.getEntityTracker.removeEntityFromAllTrackingPlayers(ent)
    currentWorld.wrapped.getPlayerManager.removePlayer(ent)
    server.getConfigurationManager.playerEntityList.remove(ent)
    currentWorld.wrapped.removePlayerEntityDangerously(ent) //Force the entity to be removed from it's current dimension

    val mappack = if(destMap.isDefined) destMap.get.mappack else null
    val pos = if(mappack == null) new Location(destWorld, 0, 64, 0) else {val l = new Location(destWorld.getConfig.spawnPoint); l.setWorld(destWorld); l}

    //TODO: implement gamemanager
    /*if(destMap.getGameManager().isGameRunning()){
      if(player.getSpawnpoint() != null){
        pos = player.getSpawnpoint();
      }else if(player.getTeam() instanceof TeamUndefined){
        if(mappack != null && mappack.getMappackMetadata().isChoosingRandomSpawnpointAtRespawn()){
          List<Location> spawnpoints = mappack.getMappackMetadata().getRandomSpawnpoints();
          pos = spawnpoints.get(NailedAPI.getMapLoader().getRandomSpawnpointSelector().nextInt(spawnpoints.size()));
        }
      }else{
        if(player.getTeam().shouldOverrideDefaultSpawnpoint()){
          pos = player.getTeam().getSpawnpoint();
        }
      }
    }*/

    ent.dimension = destWorld.getDimensionId

    val worldManager = new ItemInWorldManager(destWorld.wrapped)

    val newPlayer = new EntityPlayerMP(server, destWorld.wrapped, ent.getGameProfile, worldManager)
    newPlayer.playerNetServerHandler = ent.playerNetServerHandler
    newPlayer.clonePlayer(ent, false)
    newPlayer.dimension = destWorld.getDimensionId
    newPlayer.setEntityId(ent.getEntityId)

    worldManager.setGameType(ent.theItemInWorldManager.getGameType)
    worldManager.initializeGameType(destWorld.wrapped.getWorldInfo.getGameType)

    newPlayer.setLocationAndAngles(pos.getX, pos.getY, pos.getZ, pos.getYaw, pos.getPitch)
    destWorld.wrapped.theChunkProviderServer.loadChunk(newPlayer.posX.toInt >> 4, newPlayer.posZ.toInt >> 4)

    player.sendPacket(new S07PacketRespawn(destWorld.getConfig.dimension, destWorld.wrapped.difficultySetting, destWorld.wrapped.getWorldInfo.getTerrainType, worldManager.getGameType))
    player.netHandler.setPlayerLocation(pos.getX, pos.getY, pos.getZ, pos.getYaw, pos.getPitch)
    player.sendPacket(new S05PacketSpawnPosition(pos.getBlockX, pos.getBlockY, pos.getBlockZ))
    player.sendPacket(new S1FPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel))
    server.getConfigurationManager.updateTimeAndWeatherForPlayer(newPlayer, destWorld.wrapped)
    destWorld.wrapped.getPlayerManager.addPlayer(newPlayer)
    destWorld.wrapped.spawnEntityInWorld(newPlayer)
    server.getConfigurationManager.playerEntityList.asInstanceOf[java.util.List[EntityPlayer]].add(newPlayer)
    newPlayer.addSelfToInternalCraftingInventory()
    newPlayer.setHealth(newPlayer.getHealth)

    player.netHandler.playerEntity = newPlayer

    //TODO: respawn event
  }
}
