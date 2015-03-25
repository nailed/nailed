package jk_5.nailed.plugins.worldedit;

import com.google.common.base.Charsets;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class WorldEditPlayer extends AbstractPlayerActor {

    private final NailedPlayer player;
    private final EntityPlayerMP playerEntity;

    protected WorldEditPlayer(NailedPlayer player) {
        this.player = player;
        this.playerEntity = player.getEntity();
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public int getItemInHand() {
        ItemStack is = this.playerEntity.getCurrentEquippedItem();
        return is == null ? 0 : Item.getIdFromItem(is.getItem());
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector position = new Vector(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ);
        return new Location(
                WorldEditWorld.wrap(this.playerEntity.worldObj),
                position,
                this.playerEntity.cameraYaw,
                this.playerEntity.cameraPitch);
    }

    @Override
    public WorldVector getPosition() {
        return new WorldVector(LocalWorldAdapter.adapt(WorldEditWorld.wrap(this.playerEntity.worldObj)), this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ);
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return WorldEditWorld.wrap(this.playerEntity.worldObj);
    }

    @Override
    public double getPitch() {
        return this.playerEntity.rotationPitch;
    }

    @Override
    public double getYaw() {
        return this.playerEntity.rotationYaw;
    }

    @Override
    public void giveItem(int type, int amt) {
        this.playerEntity.inventory.addItemStackToInventory(new ItemStack(Item.getItemById(type), amt, 0));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        player.sendPluginMessage(NailedWorldEditPlugin.instance().identifier, "WECUI", send.getBytes(Charsets.UTF_8));
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(new TextComponent(part));
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            TextComponent comp = new TextComponent(part);
            comp.setColor(ChatColor.GRAY);
            this.player.sendMessage(comp);
        }
    }

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            TextComponent comp = new TextComponent(part);
            comp.setColor(ChatColor.LIGHT_PURPLE);
            this.player.sendMessage(comp);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            TextComponent comp = new TextComponent(part);
            comp.setColor(ChatColor.RED);
            this.player.sendMessage(comp);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        this.playerEntity.playerNetServerHandler.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    @Override
    public String[] getGroups() {
        return new String[]{}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.playerEntity.username);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        //return ForgeWorldEdit.inst.getPermissionsProvider().hasPermission(playerEntity, perm);
        return true; //TODO: permissions
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getUniqueId(), player.getName());
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            // We can't directly check if the player is online because
            // the list of players is not thread safe
            return ThreadSafeCache.getInstance().getOnlineIds().contains(uuid);
        }

        @Override
        public boolean isPersistent() {
            return true;
        }
    }

    @Deprecated
    public static WorldEditPlayer wrap(EntityPlayerMP player){
        return new WorldEditPlayer(NailedPlatform.instance().getPlayerFromEntity(player));
    }

    public static WorldEditPlayer wrap(NailedPlayer player){
        return new WorldEditPlayer(player);
    }
}
