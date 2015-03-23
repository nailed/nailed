package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.*;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.world.World;
import jk_5.nailed.server.world.NailedDimensionManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.*;

class NailedWorldEditPlatform extends AbstractPlatform implements MultiUserPlatform {

    private static final NailedWorldEditPlatform INSTANCE = new NailedWorldEditPlatform();

    private final WorldEditConfig config = new WorldEditConfig();
    private final MinecraftServer server = MinecraftServer.getServer();
    private boolean hookingEvents = false;

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public int resolveItem(String name) {
        if(name == null){
            return 0;
        }
        Item i = ((Item) Item.itemRegistry.getObject(name));
        if(i == null){
            i = ((Item) Item.itemRegistry.getObject("minecraft:" + name));
        }
        if(i == null){
            return 0;
        }
        return Item.getIdFromItem(i);
    }

    @Override
    public boolean isValidMobType(String type) {
        return EntityList.stringToClassMapping.containsKey(type);
    }

    @Override
    public void reload() {
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    @Override
    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        List<WorldServer> worlds = Arrays.asList(NailedDimensionManager.instance().getVanillaWorlds());
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<com.sk89q.worldedit.world.World>(worlds.size());
        for (WorldServer world : worlds) {
            ret.add(new WorldEditWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof WorldEditPlayer) {
            return player;
        } else {
            EntityPlayerMP entity = server.getConfigurationManager().getPlayerByUsername(player.getName());
            return entity != null ? new WorldEditPlayer(this, entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof WorldEditWorld) {
            return world;
        } else {
            for (WorldServer ws : NailedDimensionManager.instance().getVanillaWorlds()) {
                if (ws.getWorldInfo().getWorldName().equals(world.getName())) {
                    return new WorldEditWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(Dispatcher dispatcher) {
        if(server == null) return;
        NailedWorldEditPlugin.instance().commands = dispatcher.getCommands();
        //for (final CommandMapping command : dispatcher.getCommands()) {
            //TODO: register permissions
            /*if (command.getDescription().getPermissions().size() > 0) {
                ForgeWorldEdit.inst.getPermissionsProvider().registerPermission(wrapper, command.getDescription().getPermissions().get(0));
                for (int i = 1; i < command.getDescription().getPermissions().size(); i++) {
                    ForgeWorldEdit.inst.getPermissionsProvider().registerPermission(null, command.getDescription().getPermissions().get(i));
                }
            }*/
        //}
    }

    @Override
    public void registerGameHooks() {
        // We registered the events already anyway, so we just 'turn them on'
        hookingEvents = true;
    }

    @Override
    public WorldEditConfig getConfiguration() {
        return config;
    }

    @Override
    public String getVersion() {
        return NailedWorldEditPlugin.instance().getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getPlatformName() {
        return "Nailed";
    }

    @Override
    public String getPlatformVersion() {
        return NailedWorldEditPlugin.instance().getClass().getPackage().getImplementationVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<Capability, Preference>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.PREFERRED);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.NORMAL);
        return capabilities;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<Actor>();
        ServerConfigurationManager scm = server.getConfigurationManager();
        for (String name : scm.getAllUsernames()) {
            EntityPlayerMP entity = scm.getPlayerByUsername(name);
            if (entity != null) {
                users.add(new WorldEditPlayer(this, entity));
            }
        }
        return users;
    }

    public static NailedWorldEditPlatform instance(){
        return INSTANCE;
    }

    public static ItemStack toVanilla(BaseItemStack item) {
        ItemStack ret = new ItemStack(Item.getItemById(item.getType()), item.getAmount(), item.getData());
        for(Map.Entry<Integer, Integer> entry : item.getEnchantments().entrySet()){
            ret.addEnchantment(net.minecraft.enchantment.Enchantment.getEnchantmentById(entry.getKey()), entry.getValue());
        }

        return ret;
    }
}
