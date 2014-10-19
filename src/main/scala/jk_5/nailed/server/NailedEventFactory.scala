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
import jk_5.nailed.api.GameMode
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.event.PlatformEvent
import jk_5.nailed.api.event.player._
import jk_5.nailed.api.event.server.{ServerPostTickEvent, ServerPreTickEvent}
import jk_5.nailed.api.event.world.{WorldPostTickEvent, WorldPreTickEvent}
import jk_5.nailed.api.map.Map
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.util.Location
import jk_5.nailed.server.command.NailedCommandManager
import jk_5.nailed.server.command.sender.{CommandBlockCommandSender, ConsoleCommandSender}
import jk_5.nailed.server.event.{EntityDamageEvent, EntityFallEvent}
import jk_5.nailed.server.map.NailedMap
import jk_5.nailed.server.network.NettyChannelInitializer
import jk_5.nailed.server.player.NailedPlayer
import jk_5.nailed.server.utils.ItemStackConverter._
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.command.ICommandSender
import net.minecraft.command.server.CommandBlockLogic
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.{S05PacketSpawnPosition, S07PacketRespawn, S1FPacketSetExperience}
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.management.ItemInWorldManager
import net.minecraft.util.{BlockPos, DamageSource, EnumFacing}
import net.minecraft.world.WorldSettings.GameType
import net.minecraft.world.{World, WorldServer}
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object NailedEventFactory {

  var serverCommandSender: ConsoleCommandSender = _

  private val preTickEvent = new ServerPreTickEvent
  private val postTickEvent = new ServerPostTickEvent

  private val logger = LogManager.getLogger

  private var subtitleCounter = 0

  def fireEvent[T <: Event](event: T): T = {
    event match {
      case e: PlatformEvent if e.getPlatform == null => e.setPlatform(NailedPlatform)
      case _ =>
    }
    NailedPlatform.globalEventBus.post(event)
    event
  }

  def firePreWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPreTickEvent(NailedPlatform.getWorld(world.provider.getDimensionId)))
  }

  def firePostWorldTick(server: MinecraftServer, world: WorldServer) = {
    fireEvent(new WorldPostTickEvent(NailedPlatform.getWorld(world.provider.getDimensionId)))
  }

  def firePreServerTick(server: MinecraftServer) = fireEvent(preTickEvent)
  def firePostServerTick(server: MinecraftServer) = {
    if(subtitleCounter == 15){
      val it = NailedPlatform.getOnlinePlayers.iterator()
      while(it.hasNext){
        val p = it.next()
        val sub = p.asInstanceOf[NailedPlayer].subtitle
        if(sub != null){
          p.displaySubtitle(sub: _*)
        }
      }
      subtitleCounter = 0
    }else subtitleCounter += 1
    fireEvent(postTickEvent)
  }

  def fireServerStartBeforeConfig(server: DedicatedServer){
    NailedPlatform.preLoad(server)
    this.serverCommandSender = new ConsoleCommandSender(server)
  }

  def fireServerStarted(server: DedicatedServer){
    NettyChannelInitializer.serverStarting = false
    NailedPlatform.load(server)
  }

  def fireStartBeforeWorldLoad(server: DedicatedServer){

  }

  def fireCommand(sender: ICommandSender, input: String): Int = {
    val wrapped = sender match {
      case p: EntityPlayerMP => NailedPlatform.getPlayer(p.getGameProfile.getId)
      case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      //case r: RConConsoleSource => new RConCommandSender(r)
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped == null) return -1
    NailedCommandManager.fireCommand(if(wrapped.isInstanceOf[Player]) input.substring(1) else input, wrapped, _.put(classOf[ICommandSender], sender))
    1
  }

  def fireTabCompletion(sender: ICommandSender, input: String): util.List[String] = {
    val wrapped = sender match {
      case p: EntityPlayerMP => NailedPlatform.getPlayer(p.getGameProfile.getId)
      case c: CommandBlockLogic => new CommandBlockCommandSender(c) //TODO: use our own api commandblock for this
      //case r: RConConsoleSource => new RConCommandSender(r)
      case s: MinecraftServer => this.serverCommandSender
      case _ => null
    }
    if(wrapped != null) NailedCommandManager.fireAutocompletion(input, wrapped, _.put(classOf[ICommandSender], sender)) else null
  }

  def fireWorldLoad(world: World){

  }

  def fireWorldUnload(world: World){

  }

  def fireEntityInPortal(entity: Entity){

  }

  def firePlayerJoined(playerEntity: EntityPlayerMP){
    val player = NailedPlatform.getOrCreatePlayer(playerEntity).asInstanceOf[NailedPlayer]
    player.entity = playerEntity
    player.isOnline = true
    player.world = NailedPlatform.getWorld(playerEntity.dimension)
    player.map = player.world.getMap
    player.netHandler = playerEntity.playerNetServerHandler
    player.world.onPlayerJoined(player)
    Option(player.world.getMap).foreach{
      case m: NailedMap => m.onPlayerJoined(player)
      case _ =>
    }
    player.getScoreboardManager.onJoinedServer()
    player.sendSupportedChannels()
    val e = this.fireEvent(new PlayerJoinServerEvent(player))
    NailedPlatform.broadcastMessage(e.getMessage)
    if(player.map != null) this.firePlayerJoinMap(player, player.map)
    this.firePlayerJoinWorld(player, player.world)
  }

  def firePlayerLeft(playerEntity: EntityPlayerMP){
    val player = NailedPlatform.getPlayerFromEntity(playerEntity).asInstanceOf[NailedPlayer]
    player.isOnline = false
    val e = this.fireEvent(new PlayerLeaveServerEvent(player))
    if(player.map != null) this.firePlayerLeftMap(player, player.map)
    this.firePlayerLeftWorld(player, player.world)
    player.getScoreboardManager.onLeftServer()
    player.world.onPlayerLeft(player)
    Option(player.world.getMap).foreach{
      case m: NailedMap => m.onPlayerLeft(player)
      case _ =>
    }
    player.entity = null
    player.world = null
    player.map = null
    player.netHandler = null
    NailedPlatform.broadcastMessage(e.getMessage)
  }

  def firePlayerChat(playerEntity: EntityPlayerMP, message: String): String = {
    val player = NailedPlatform.getPlayerFromEntity(playerEntity)
    val e = this.fireEvent(new PlayerChatEvent(player, message))
    if(e.isCanceled) null else e.getMessage
  }

  def fireOnRightClick(playerEntity: EntityPlayer, world: World, is: ItemStack, pos: BlockPos, side: EnumFacing, bX: Float, bY: Float, bZ: Float): Boolean = {
    val player = NailedPlatform.getPlayerFromEntity(playerEntity.asInstanceOf[EntityPlayerMP]).asInstanceOf[NailedPlayer]
    val xC = pos.getX + side.getFrontOffsetX
    val yC = pos.getY + side.getFrontOffsetY
    val zC = pos.getZ + side.getFrontOffsetZ
    //TODO
    val canceled = false//fireEvent(new BlockPlaceEvent(xC, yC, zC, NailedDimensionManager.getWorld(world.provider.getDimensionId), player)).isCanceled
    val ret = if(canceled){
      //Send the slot content to the client because the client decreases the stack size by 1 when it places a block
      player.getEntity.sendContainerAndContentsToPlayer(player.getEntity.inventoryContainer, player.getEntity.inventoryContainer.getInventory)
      true
    }else false

    if(ret) return true

    //TODO: allow to do this in an event handler
    if(is.getTagCompound != null && is.getTagCompound.getBoolean("IsStatemitter")){
      if(player.getGameMode != GameMode.CREATIVE){
        player.sendMessage(new ComponentBuilder("You must be in creative mode to use Stat Emitters!").color(ChatColor.RED).create(): _*)
        player.getEntity.sendContainerAndContentsToPlayer(player.getEntity.inventoryContainer, player.getEntity.inventoryContainer.getInventory)
        return true
      }
      /*world.func_175722_b(new BlockPos(xC, yC, zC), Blocks.command_block)
      world.setBlock(xC, yC, zC, Blocks.command_block, 8, 3)
      if(is.getTagCompound.hasKey("Content")){
        world.getTileEntity(xC, yC, zC).asInstanceOf[TileEntityStatEmitter].commandBlockLogic.setCommand(is.getTagCompound.getString("Content"))
      }
      return true*/
    }
    ret
  }

  def fireOnBlockBroken(world: World, gameType: GameType, playerEntity: EntityPlayerMP, x: Int, y: Int, z: Int): Boolean = {
    var preCancel = false
    /*if(gameType.isAdventure && !playerEntity.isCurrentToolAdventureModeExempt(x, y, z)){
      preCancel = true
    }else if(gameType.isCreative && playerEntity.getHeldItem != null && playerEntity.getHeldItem.getItem.isInstanceOf[ItemSword]){
      preCancel = true
    }*/

    // Tell client the block is gone immediately then process events
    /*if(world.getTileEntity(x, y, z) == null){
      val packet = new S23PacketBlockChange(x, y, z, world)
      packet.field_148883_d = Blocks.air
      packet.field_148884_e = 0
      playerEntity.playerNetServerHandler.sendPacket(packet)
    }*/

    //Post the block break event
    /*val player = Server.getInstance.getPlayer(playerEntity.getGameProfile.getId).get
    val block = world.getBlock(x, y, z)
    val meta = world.getBlockMetadata(x, y, z)
    val event = new BlockBreakEvent(x, y, z, NailedDimensionManager.getWorld(world.provider.func_177502_q()), Material.getMaterial(Block.getIdFromBlock(block)), meta.toByte, player)
    event.setCanceled(preCancel)
    fireEvent(event)*/

    /*if(event.isCanceled){
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
    }else false*/
    false
  }

  def firePlayerDropStack(player: EntityPlayerMP, fullStack: Boolean): Boolean = {
    //Return false to cancel
    !fireEvent(new PlayerThrowItemEvent(NailedPlatform.getPlayerFromEntity(player), player.getCurrentEquippedItem)).isCanceled
  }

  def firePlayerLeftWorld(player: Player, world: api.world.World) = fireEvent(new PlayerLeaveWorldEvent(player, world))
  def firePlayerJoinWorld(player: Player, world: api.world.World) = fireEvent(new PlayerJoinWorldEvent(player, world))
  def firePlayerLeftMap(player: Player, world: Map) = fireEvent(new PlayerLeaveMapEvent(player, world))
  def firePlayerJoinMap(player: Player, world: Map) = fireEvent(new PlayerJoinMapEvent(player, world))

  def fireOnItemRightClick(player: EntityPlayer, world: World, stack: ItemStack): Boolean = {
    fireEvent(new PlayerRightClickItemEvent(NailedPlatform.getPlayerFromEntity(player.asInstanceOf[EntityPlayerMP]), stack)).isCanceled
  }

  def onPlayerRespawn(ent: EntityPlayerMP){
    val server = MinecraftServer.getServer
    val player = NailedPlatform.getPlayerFromEntity(ent).asInstanceOf[NailedPlayer]
    val destWorld = NailedDimensionManager.getWorld(ent.dimension)
    val currentWorld = NailedDimensionManager.getWorld(ent.dimension)
    val destMap = destWorld.getMap

    currentWorld.wrapped.getEntityTracker.removePlayerFromTrackers(ent) //Remove from EntityTracker
    currentWorld.wrapped.getEntityTracker.untrackEntity(ent) //Notify other players of entity death
    currentWorld.wrapped.getPlayerManager.removePlayer(ent) //Remove player's ChunkLoader
    server.getConfigurationManager.playerEntityList.remove(ent) //Remove from the global player list
    currentWorld.wrapped.removePlayerEntityDangerously(ent) //Force the entity to be removed from it's current world

    val mappack = if(destMap != null) destMap.mappack else null
    val pos = if(mappack == null) new Location(destWorld, 0, 64, 0) else Location.builder.copy(destWorld.getConfig.spawnPoint).setWorld(destWorld).build()

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

    newPlayer.setLocationAndAngles(pos.getX, pos.getY, pos.getZ, pos.getYaw, pos.getPitch)
    destWorld.wrapped.theChunkProviderServer.loadChunk(newPlayer.posX.toInt >> 4, newPlayer.posZ.toInt >> 4)

    player.sendPacket(new S07PacketRespawn(destWorld.getConfig.dimension.getId, destWorld.wrapped.getDifficulty, destWorld.wrapped.getWorldInfo.getTerrainType, worldManager.getGameType))
    player.netHandler.setPlayerLocation(pos.getX, pos.getY, pos.getZ, pos.getYaw, pos.getPitch)
    player.sendPacket(new S05PacketSpawnPosition(new BlockPos(pos.getX, pos.getY, pos.getZ)))
    player.sendPacket(new S1FPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel))
    server.getConfigurationManager.updateTimeAndWeatherForPlayer(newPlayer, destWorld.wrapped)
    destWorld.wrapped.getPlayerManager.addPlayer(newPlayer)
    destWorld.wrapped.spawnEntityInWorld(newPlayer)
    server.getConfigurationManager.playerEntityList.asInstanceOf[java.util.List[EntityPlayer]].add(newPlayer)
    newPlayer.addSelfToInternalCraftingInventory()
    newPlayer.setHealth(newPlayer.getHealth)

    player.netHandler.playerEntity = newPlayer
    player.entity = newPlayer

    //TODO: respawn event
  }

  def onLivingFall(entity: EntityLivingBase, distance: Float): Float = {
    val event = new EntityFallEvent(entity, distance) //TODO: api event
    if(fireEvent(event).isCanceled) 0 else event.distance
  }

  def onEntityDamage(entity: EntityLivingBase, source: DamageSource, amount: Float): Float = {
    val event = new EntityDamageEvent(entity, source, amount) //TODO: api event
    if(fireEvent(event).isCanceled) 0 else event.amount
  }

  def firePlayerRightClickAir(player: EntityPlayerMP): Boolean = { //return true to cancel
    val event = new PlayerInteractEvent(NailedPlatform.getPlayerFromEntity(player), PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
    fireEvent(event).isCanceled
  }

  def firePlayerRightClickBlock(player: EntityPlayerMP, x: Int, y: Int, z: Int): Boolean = { //return true to cancel
    val event = new PlayerInteractEvent(NailedPlatform.getPlayerFromEntity(player), PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, new Location(x, y, z)) //TODO: include world
    fireEvent(event).isCanceled
  }

  def firePlayerLeftClickAir(player: EntityPlayerMP){
    val event = new PlayerInteractEvent(NailedPlatform.getPlayerFromEntity(player), PlayerInteractEvent.Action.LEFT_CLICK_AIR)
    fireEvent(event)
  }

  def firePlayerLeftClickBlock(player: EntityPlayerMP, x: Int, y: Int, z: Int): Boolean = { //return true to cancel
    val event = new PlayerInteractEvent(NailedPlatform.getPlayerFromEntity(player), PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, new Location(x, y, z)) //TODO: include world
    fireEvent(event).isCanceled
  }

  def firePlayerRegisterChannelEvent(player: NailedPlayer, channel: String){
    //TODO
  }

  def firePlayerUnregisterChannelEvent(player: NailedPlayer, channel: String){
    //TODO
  }
}
