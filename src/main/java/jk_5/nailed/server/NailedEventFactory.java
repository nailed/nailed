package jk_5.nailed.server;

import jk_5.eventbus.Event;
import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.event.PlatformEvent;
import jk_5.nailed.api.event.player.*;
import jk_5.nailed.api.event.server.ServerPostTickEvent;
import jk_5.nailed.api.event.server.ServerPreTickEvent;
import jk_5.nailed.api.event.world.WorldPostTickEvent;
import jk_5.nailed.api.event.world.WorldPreTickEvent;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.server.command.NailedCommandManager;
import jk_5.nailed.server.command.sender.CommandBlockCommandSender;
import jk_5.nailed.server.command.sender.ConsoleCommandSender;
import jk_5.nailed.server.event.EntityDamageEvent;
import jk_5.nailed.server.event.EntityFallEvent;
import jk_5.nailed.server.map.NailedMap;
import jk_5.nailed.server.network.NettyChannelInitializer;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.scoreboard.PlayerScoreboardManager;
import jk_5.nailed.server.tileentity.TileEntityStatEmitter;
import jk_5.nailed.server.utils.ItemStackConverter;
import jk_5.nailed.server.world.NailedDimensionManager;
import jk_5.nailed.server.world.NailedWorld;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NailedEventFactory {

    private static final ServerPreTickEvent preTickEvent = new ServerPreTickEvent();
    private static final ServerPostTickEvent postTickEvent = new ServerPostTickEvent();
    private static final Logger logger = LogManager.getLogger();

    private static int subtitleCounter;

    public static ConsoleCommandSender serverCommandSender;

    public static <T extends Event> T fireEvent(T event){
        if(event instanceof PlatformEvent){
            if(((PlatformEvent) event).getPlatform() == null){
                ((PlatformEvent) event).setPlatform(NailedPlatform.instance());
            }
        }
        NailedPlatform.instance().getEventBus().post(event);
        return event;
    }

    public static WorldPreTickEvent firePreWorldTick(MinecraftServer server, WorldServer world){
        return fireEvent(new WorldPreTickEvent(NailedPlatform.instance().getWorld(world.provider.getDimensionId())));
    }

    public static WorldPostTickEvent firePostWorldTick(MinecraftServer server, WorldServer world){
        return fireEvent(new WorldPostTickEvent(NailedPlatform.instance().getWorld(world.provider.getDimensionId())));
    }

    public static ServerPreTickEvent firePreServerTick(MinecraftServer server){
        return fireEvent(preTickEvent);
    }

    public static ServerPostTickEvent firePostServerTick(MinecraftServer server){
        if(subtitleCounter == 15){
            for (Player player : NailedPlatform.instance().getOnlinePlayers()) {
                NailedPlayer p = ((NailedPlayer) player);
                BaseComponent[] sub = p.getSubtitle();
                if(sub != null){
                    p.displaySubtitle(sub);
                }
            }
            subtitleCounter = 0;
        }else{
            subtitleCounter += 1;
        }
        return fireEvent(postTickEvent);
    }

    public static void fireServerStartBeforeConfig(DedicatedServer server){
        NailedPlatform.instance().preLoad(server);
        serverCommandSender = new ConsoleCommandSender();
    }

    public static void fireServerStarted(DedicatedServer server){
        NettyChannelInitializer.serverStarting = false;
        NailedPlatform.instance().load();
    }

    public static void fireStartBeforeWorldLoad(DedicatedServer server){

    }

    public static int fireCommand(ICommandSender sender, String input){
        CommandSender wrapped = null;
        if(sender instanceof EntityPlayerMP){
            wrapped = NailedPlatform.instance().getPlayer(((EntityPlayerMP) sender).getGameProfile().getId());
        }else if(sender instanceof CommandBlockLogic){
            wrapped = new CommandBlockCommandSender(((CommandBlockLogic) sender)); //TODO: replace this with our own api
        }else if(sender instanceof RConConsoleSource){
            //wrapped = new RConCommandSender(((RConConsoleSource) sender));
        }else if(sender instanceof MinecraftServer){
            wrapped = serverCommandSender;
        }
        if(wrapped == null){
            return -1;
        }
        return NailedCommandManager.fireCommand((wrapped instanceof Player) ? input.substring(1) : input, wrapped, sender);
    }

    public static List<String> fireTabCompletion(ICommandSender sender, String input){
        CommandSender wrapped = null;
        if(sender instanceof EntityPlayerMP){
            wrapped = NailedPlatform.instance().getPlayer(((EntityPlayerMP) sender).getGameProfile().getId());
        }else if(sender instanceof CommandBlockLogic){
            wrapped = new CommandBlockCommandSender(((CommandBlockLogic) sender)); //TODO: replace this with our own api
        }else if(sender instanceof RConConsoleSource){
            //wrapped = new RConCommandSender(((RConConsoleSource) sender));
        }else if(sender instanceof MinecraftServer){
            wrapped = serverCommandSender;
        }
        if(wrapped == null){
            return null;
        }
        return NailedCommandManager.fireAutocompletion(input, wrapped, sender);
    }

    public static void fireWorldLoad(World world){

    }

    public static void fireWorldUnload(World world){

    }

    public static void fireEntityInPortal(Entity entity){

    }

    public static void firePlayerJoined(EntityPlayerMP playerEntity){
        NailedPlayer player = NailedPlatform.instance().getOrCreatePlayer(playerEntity);
        player.entity = playerEntity;
        player.isOnline = true;
        player.world = NailedPlatform.instance().getWorld(playerEntity.dimension);
        player.map = player.world.getMap();
        player.netHandler = playerEntity.playerNetServerHandler;
        player.world.onPlayerJoined(player);
        if(player.map != null){
            ((NailedMap) player.map).onPlayerJoined(player);
        }
        ((PlayerScoreboardManager) player.getScoreboardManager()).onJoinedServer();
        player.sendSupportedChannels();
        PlayerJoinServerEvent e = fireEvent(new PlayerJoinServerEvent(player));
        NailedPlatform.instance().broadcastMessage(e.getMessage());
        if(player.map != null){
            firePlayerJoinMap(player, player.map);
        }
        firePlayerJoinWorld(player, player.world);
    }

    public static void firePlayerLeft(EntityPlayerMP playerEntity){
        NailedPlayer player = NailedPlatform.instance().getOrCreatePlayer(playerEntity);
        player.isOnline = false;
        PlayerLeaveServerEvent e = fireEvent(new PlayerLeaveServerEvent(player));
        if(player.map != null){
            firePlayerLeftMap(player, player.map);
        }
        firePlayerLeftWorld(player, player.world);
        ((PlayerScoreboardManager) player.getScoreboardManager()).onLeftServer();
        player.world.onPlayerLeft(player);
        if(player.map != null){
            ((NailedMap) player.map).onPlayerLeft(player);
        }
        player.entity = null;
        player.world = null;
        player.map = null;
        player.netHandler = null;
        NailedPlatform.instance().broadcastMessage(e.getMessage());
    }

    public static String firePlayerChat(EntityPlayerMP playerEntity, String message){
        Player player = NailedPlatform.instance().getPlayerFromEntity(playerEntity);
        PlayerChatEvent e = fireEvent(new PlayerChatEvent(player, message));
        return e.isCanceled() ? null : e.getMessage();
    }

    public static boolean fireOnRightClick(EntityPlayer playerEntity, World world, ItemStack is, BlockPos pos, EnumFacing side, float bX, float bY, float bZ){
        NailedPlayer player = ((NailedPlayer) NailedPlatform.instance().getPlayerFromEntity(((EntityPlayerMP) playerEntity)));
        int xC = pos.getX() + side.getFrontOffsetX();
        int yC = pos.getY() + side.getFrontOffsetY();
        int zC = pos.getZ() + side.getFrontOffsetZ();
        //TODO
        boolean canceled = false;//fireEvent(new BlockPlaceEvent(xC, yC, zC, NailedDimensionManager.getWorld(world.provider.getDimensionId), player)).isCanceled
        boolean ret;
        if(canceled){
            //Send the slot content to the client because the client decreases the stack size by 1 when it places a block
            //was sendContainerAndContentsToPlayer
            player.getEntity().updateCraftingInventory(player.getEntity().inventoryContainer, player.getEntity().inventoryContainer.getInventory());
            ret = true;
        }else{
            ret = false;
        }

        if(ret){
            return true;
        }

        //TODO: allow to do this in an event handler
        if(is.getTagCompound() != null && is.getTagCompound().getBoolean("IsStatemitter")){
            if(player.getGameMode() != GameMode.CREATIVE){
                player.sendMessage(new ComponentBuilder("You must be in creative mode to use Stat Emitters!").color(ChatColor.RED).create());
                //was sendContainerAndContentsToPlayer
                player.getEntity().updateCraftingInventory(player.getEntity().inventoryContainer, player.getEntity().inventoryContainer.getInventory());
                return true;
            }
            BlockPos pos1 = new BlockPos(xC, yC, zC);
            world.setBlockState(pos1, Blocks.command_block.getDefaultState());
            world.setTileEntity(pos1, new TileEntityStatEmitter());
            if(is.getTagCompound().hasKey("Content")){
                ((TileEntityStatEmitter) world.getTileEntity(pos1)).getCommandBlockLogic().setCommand(is.getTagCompound().getString("Content"));
            }
            return true;
        }
        return ret;
    }

    public static boolean fireOnBlockBroken(World world, WorldSettings.GameType gameType, EntityPlayerMP playerEntity, int x, int y, int z){
        boolean preCancel = false;
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
        return false;
    }

    public static boolean firePlayerDropStack(EntityPlayerMP player, boolean fullStack){
        //Return false to cancel
        return !fireEvent(new PlayerThrowItemEvent(NailedPlatform.instance().getPlayerFromEntity(player), ItemStackConverter.toNailed(player.getCurrentEquippedItem()))).isCanceled();
    }

    public static PlayerLeaveWorldEvent firePlayerLeftWorld(Player player, jk_5.nailed.api.world.World world){
        return fireEvent(new PlayerLeaveWorldEvent(player, world));
    }

    public static PlayerJoinWorldEvent firePlayerJoinWorld(Player player, jk_5.nailed.api.world.World world){
        return fireEvent(new PlayerJoinWorldEvent(player, world));
    }

    public static PlayerLeaveMapEvent firePlayerLeftMap(Player player, Map map){
        return fireEvent(new PlayerLeaveMapEvent(player, map));
    }

    public static PlayerJoinMapEvent firePlayerJoinMap(Player player, Map map){
        return fireEvent(new PlayerJoinMapEvent(player, map));
    }

    public static boolean fireOnItemRightClick(EntityPlayer player, World world, ItemStack stack){
        return fireEvent(new PlayerRightClickItemEvent(NailedPlatform.instance().getPlayerFromEntity(((EntityPlayerMP) player)), ItemStackConverter.toNailed(stack))).isCanceled();
    }

    public static void onPlayerRespawn(EntityPlayerMP ent){
        MinecraftServer server = MinecraftServer.getServer();
        NailedPlayer player = ((NailedPlayer) NailedPlatform.instance().getPlayerFromEntity(ent));
        NailedWorld destWorld = NailedDimensionManager.instance().getWorld(ent.dimension);
        NailedWorld currentWorld = NailedDimensionManager.instance().getWorld(ent.dimension);
        Map destMap = destWorld.getMap();

        currentWorld.getWrapped().getEntityTracker().removePlayerFromTrackers(ent); //Remove from EntityTracker
        currentWorld.getWrapped().getEntityTracker().untrackEntity(ent); //Notify other players of entity death
        currentWorld.getWrapped().getPlayerManager().removePlayer(ent); //Remove player's ChunkLoader
        server.getConfigurationManager().playerEntityList.remove(ent); //Remove from the global player list
        currentWorld.getWrapped().removePlayerEntityDangerously(ent); //Force the entity to be removed from it's current world

        Mappack mappack = destMap != null ? destMap.mappack() : null;
        Location pos = mappack == null ? new Location(destWorld, 0, 64, 0) : Location.builder().copy(destWorld.getConfig().spawnPoint()).setWorld(destWorld).build();

        if(destMap != null && destMap.getGameManager().isGameRunning()){
            if(destMap.getPlayerTeam(player) == null){
                //TODO: random spawnpoints
                /*if(mappack != null && mappack.getMetadata.isChoosingRandomSpawnpointAtRespawn()){
                  List<Location> spawnpoints = mappack.getMappackMetadata().getRandomSpawnpoints();
                  pos = spawnpoints.get(NailedAPI.getMapLoader().getRandomSpawnpointSelector().nextInt(spawnpoints.size()));
                }*/
            }else{
                Location p = destMap.getPlayerTeam(player).getSpawnPoint();
                if(p != null) pos = p;
            }
        }

        ent.dimension = destWorld.getDimensionId();

        ItemInWorldManager worldManager = new ItemInWorldManager(destWorld.getWrapped());

        EntityPlayerMP newPlayer = new EntityPlayerMP(server, destWorld.getWrapped(), ent.getGameProfile(), worldManager);
        newPlayer.playerNetServerHandler = ent.playerNetServerHandler;
        newPlayer.clonePlayer(ent, false);
        newPlayer.dimension = destWorld.getDimensionId();
        newPlayer.setEntityId(ent.getEntityId());

        worldManager.setGameType(ent.theItemInWorldManager.getGameType());

        newPlayer.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        destWorld.getWrapped().theChunkProviderServer.loadChunk((int)(newPlayer.posX) >> 4, (int)(newPlayer.posZ) >> 4);

        player.sendPacket(new S07PacketRespawn(destWorld.getConfig().dimension().getId(), destWorld.getWrapped().getDifficulty(), destWorld.getWrapped().getWorldInfo().getTerrainType(), worldManager.getGameType()));
        player.netHandler.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        player.sendPacket(new S05PacketSpawnPosition(new BlockPos(pos.getX(), pos.getY(), pos.getZ())));
        player.sendPacket(new S1FPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));
        server.getConfigurationManager().updateTimeAndWeatherForPlayer(newPlayer, destWorld.getWrapped());
        destWorld.getWrapped().getPlayerManager().addPlayer(newPlayer);
        destWorld.getWrapped().spawnEntityInWorld(newPlayer);
        //noinspection unchecked
        ((List<EntityPlayer>) server.getConfigurationManager().playerEntityList).add(newPlayer);
        newPlayer.addSelfToInternalCraftingInventory();
        newPlayer.setHealth(newPlayer.getHealth());

        player.netHandler.playerEntity = newPlayer;
        player.entity = newPlayer;

        //TODO: respawn event
    }

    public static float onLivingFall(EntityLivingBase entity, Float distance){
        EntityFallEvent event = new EntityFallEvent(entity, distance); //TODO: api event
        return fireEvent(event).isCanceled() ? 0 : event.getDistance();
    }

    public static float onEntityDamage(EntityLivingBase entity, DamageSource source, float amount){
        EntityDamageEvent event = new EntityDamageEvent(entity, source, amount); //TODO: api event
        return fireEvent(event).isCanceled() ? 0 : event.getAmount();
    }

    public static boolean firePlayerRightClickAir(EntityPlayerMP player){ //return true to cancel
        PlayerInteractEvent event = new PlayerInteractEvent(NailedPlatform.instance().getPlayerFromEntity(player), PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
        return fireEvent(event).isCanceled();
    }

    public static boolean firePlayerRightClickBlock(EntityPlayerMP player, int x, int y, int z){ //return true to cancel
        PlayerInteractEvent event = new PlayerInteractEvent(NailedPlatform.instance().getPlayerFromEntity(player), PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, new Location(x, y, z)); //TODO: include world
        return fireEvent(event).isCanceled();
    }

    public static void firePlayerLeftClickAir(EntityPlayerMP player){
        PlayerInteractEvent event = new PlayerInteractEvent(NailedPlatform.instance().getPlayerFromEntity(player), PlayerInteractEvent.Action.LEFT_CLICK_AIR);
        fireEvent(event);
    }

    public static boolean firePlayerLeftClickBlock(EntityPlayerMP player, int x, int y, int z){ //return true to cancel
        PlayerInteractEvent event = new PlayerInteractEvent(NailedPlatform.instance().getPlayerFromEntity(player), PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, new Location(x, y, z)); //TODO: include world
        return fireEvent(event).isCanceled();
    }

    public static void firePlayerRegisterChannelEvent(NailedPlayer player, String channel){
        //TODO
    }

    public static void firePlayerUnregisterChannelEvent(NailedPlayer player, String channel){
        //TODO
    }
}
