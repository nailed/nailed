package jk_5.nailed.server.player;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.ClickEvent;
import jk_5.nailed.api.chat.HoverEvent;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.chat.serialization.ComponentSerializer;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.math.EulerDirection;
import jk_5.nailed.api.math.Vector3d;
import jk_5.nailed.api.math.Vector3f;
import jk_5.nailed.api.messaging.StandardMessenger;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.plugin.PluginIdentifier;
import jk_5.nailed.api.potion.Potion;
import jk_5.nailed.api.potion.PotionEffect;
import jk_5.nailed.api.scoreboard.ScoreboardManager;
import jk_5.nailed.api.util.Checks;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.util.TeleportOptions;
import jk_5.nailed.api.util.TitleMessage;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.NailedEventFactory;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.scoreboard.PlayerScoreboardManager;
import jk_5.nailed.server.teleport.Teleporter;
import jk_5.nailed.server.world.NailedWorld;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NailedPlayer implements Player {

    private static final Logger logger = LogManager.getLogger();

    private final UUID uuid;
    private final String name;
    private final PlayerScoreboardManager scoreboardManager;
    private final Set<String> channels = new HashSet<String>();

    private EntityPlayerMP entity;
    private String displayName;
    private NetHandlerPlayServer netHandler;
    private boolean isAllowedToFly;
    private boolean isOnline = false;
    private BaseComponent[] subtitle;

    public World world;
    public Map map;

    public NailedPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.displayName = name;
        this.scoreboardManager = new PlayerScoreboardManager(this);
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public void sendMessage(BaseComponent... component) {
        if(this.netHandler != null){
            this.netHandler.sendPacket(new S02PacketChat(component));
        }
    }

    @Override
    public void heal(double amount) {
        double newAmount = this.getHealth() + amount;
        this.setHealth(Math.min(this.getMaxHealth(), newAmount));
    }

    @Override
    public double getMaxHealth() {
        return this.entity.getMaxHealth(); //TODO: this does not return the adjusted value (see setMaxHealth)
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        Checks.positive(maxHealth, "Max health must be greater than 0");
        entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth);
        if(this.getHealth() > this.getMaxHealth()){
            setHealth(maxHealth);
        }
    }

    @Override
    public void resetMaxHealth() {
        this.setMaxHealth(this.entity.getMaxHealth());
    }

    @Override
    public boolean isBurning() {
        return this.entity.fire != 0;
    }

    @Override
    public int getBurnDuration() {
        return this.entity.fire;
    }

    @Override
    public void setBurnDuration(int ticks) {
        this.entity.fire = ticks;
    }

    @Override
    public double getExperience() {
        return this.entity.experience;
    }

    @Override
    public int getLevel() {
        return this.entity.experienceLevel;
    }

    @Override
    public void setExperience(double experience) {
        this.entity.experience = (float) experience;
    }

    @Override
    public void setLevel(int level) {
        this.entity.experienceLevel = level;
    }

    @Override
    public void damage(double amount) {
        this.setHealth(this.getHealth() - amount);
    }

    @Override
    public double getHealth() {
        return Math.min(Math.max(0, entity.getHealth()), this.getMaxHealth());
    }

    @Override
    public void setHealth(double health) {
        Checks.positiveOrZero(health, "health");
        Checks.smallerThanOrEqual(health, getMaxHealth(), "health");
        if(health == 0){
            entity.onDeath(DamageSource.generic);
        }
        entity.setHealth((float) health);
    }

    @Nonnull
    @Override
    public Vector3d getPosition() {
        return this.getLocation();
    }

    @Override
    public void setPosition(@Nonnull Vector3d position) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Vector3f getVectorRotation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVectorRotation(@Nonnull Vector3f rotation) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public EulerDirection getRotation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRotation(@Nonnull EulerDirection rotation) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Vector3f getVelocity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVelocity(@Nonnull Vector3f velocity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSaturation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSaturation(double saturation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getHunger() {
        return 20 + entity.getFoodStats().getFoodLevel();
    }

    @Override
    public void setHunger(double hunger) {
        entity.getFoodStats().setFoodLevel(20 - (int) hunger);
    }

    @Override
    public void teleportTo(World world) {
        Teleporter.teleportPlayer(this, new TeleportOptions(world.getConfig() != null ? Location.builder().copy(world.getConfig().spawnPoint()).setWorld(world).build() : new Location(world, 0, 64, 0)));
    }

    public EntityPlayerMP getEntity(){
        return this.entity;
    }

    @Nullable
    @Override
    public Map getMap() {
        return this.map;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    public Location getLocation(){
        return new Location(this.world, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
    }

    public void sendPacket(Packet packet){
        if(this.netHandler != null){
            this.netHandler.sendPacket(packet);
        }
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.byId(this.entity.theItemInWorldManager.getGameType().getID());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        entity.setGameType(WorldSettings.GameType.getByID(gameMode.getId()));
    }

    @Override
    public void setAllowedToFly(boolean allowed) {
        this.isAllowedToFly = allowed;
        this.entity.capabilities.allowFlying = allowed;
        this.entity.capabilities.isFlying = allowed;
        this.entity.sendPlayerAbilities();
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

    @Override
    public void kick(@Nonnull String reason) {
        Checks.notNull(reason, "Reason may not be null");
        if(this.netHandler != null){
            this.netHandler.kickPlayerFromServer(reason);
        }
    }

    @Override
    public BaseComponent getDescriptionComponent() {
        TextComponent comp = new TextComponent(this.getDisplayName());
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getUniqueId().toString())));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
        return comp;
    }

    @Override
    public void clearAllEffects() {
        this.entity.clearActivePotions();
    }

    @Override
    public void addPotionEffect(@Nonnull PotionEffect effect) {
        this.entity.addPotionEffect(new net.minecraft.potion.PotionEffect(effect.getPotion().getId(), effect.getDuration(), effect.getLevel() - 1, effect.isAmbient(), effect.isShowParticles()));
    }

    @Override
    public void removePotionEffect(@Nonnull Potion potion) {
        this.entity.removePotionEffect(potion.getId());
    }

    @Nonnull
    @Override
    public Collection<PotionEffect> getActiveEffects() {
        ImmutableSet.Builder<PotionEffect> newCollection = ImmutableSet.builder();
        for (net.minecraft.potion.PotionEffect effect : ((Collection<net.minecraft.potion.PotionEffect>) this.entity.getActivePotionEffects())) {
            PotionEffect.Builder builder = PotionEffect.builder();
            Potion pot = Potion.byId(effect.getPotionID());
            if(pot != null){
                builder.setPotion(pot);
                builder.setAmbient(effect.getIsAmbient());
                builder.setShowParticles(effect.getIsShowParticles());
                builder.setLevel(effect.getAmplifier() + 1);
                builder.setDuration(effect.getDuration());
                newCollection.add(builder.build());
            }
        }
        return newCollection.build();
    }

    @Override
    public void loadResourcePack(String url, String hash) {
        this.entity.loadResourcePack(url, hash);
    }

    @Override
    public void sendPluginMessage(PluginIdentifier source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(NailedPlatform.instance().getMessenger(), source, channel, message);
        if(netHandler == null){
            return;
        }
        if(channels.contains(channel)){
            this.sendPacket(new S3FPacketCustomPayload(channel, Unpooled.copiedBuffer(message)));
        }
    }

    public void sendSupportedChannels(){
        if(netHandler == null){
            return;
        }
        Set<String> listening = NailedPlatform.instance().getMessenger().getIncomingChannels();
        if(!listening.isEmpty()){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            for (String channel : listening) {
                try{
                    stream.write(channel.getBytes(CharsetUtil.UTF_8));
                    stream.write((byte) 0);
                }catch(IOException e){
                    logger.error("Could not send Plugin Channel REGISTER to " + getName(), e);
                }
            }
            sendPacket(new S3FPacketCustomPayload("REGISTER", Unpooled.copiedBuffer(stream.toByteArray())));
        }
    }

    public void addChannel(String channel){
        if(channels.add(channel)){
            NailedEventFactory.firePlayerRegisterChannelEvent(this, channel);
        }
    }

    public void removeChannel(String channel){
        if(channels.remove(channel)){
            NailedEventFactory.firePlayerUnregisterChannelEvent(this, channel);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels(){
        return ImmutableSet.copyOf(channels);
    }

    @Override
    public void displayTitle(@Nonnull TitleMessage title) {
        IChatComponent main = (title.getTitle() != null && title.getTitle().length != 0) ? IChatComponent.Serializer.jsonToComponent(ComponentSerializer.toString(title.getTitle())) : null;
        IChatComponent sub = (title.getSubtitle() != null && title.getSubtitle().length != 0) ? IChatComponent.Serializer.jsonToComponent(ComponentSerializer.toString(title.getSubtitle())) : null;
        sendPacket(new S45PacketTitle(title.getFadeInTime(), title.getDisplayTime(), title.getFadeOutTime()));
        if(main != null){
            this.sendPacket(new S45PacketTitle(S45PacketTitle.Type.TITLE, main));
        }
        if(sub != null){
            this.sendPacket(new S45PacketTitle(S45PacketTitle.Type.SUBTITLE, sub));
        }
    }

    @Override
    public void clearTitle() {
        this.sendPacket(new S45PacketTitle(S45PacketTitle.Type.CLEAR, null));
    }

    @Override
    public void displaySubtitle(BaseComponent... message) {
        this.sendPacket(new S02PacketChat((byte) 2, message));
    }

    @Override
    public void setSubtitle(BaseComponent... message) {
        this.displaySubtitle(message);
        this.subtitle = message;
    }

    @Override
    public void clearSubtitle() {
        this.subtitle = null;
    }

    @Override
    public void clearInventory() {
        entity.inventory.clear();
        //was sendContainerAndContentsToPlayer
        entity.updateCraftingInventory(entity.inventoryContainer, entity.inventoryContainer.getInventory());
    }

    @Override
    public boolean isOnline() {
        return this.isOnline;
    }

    public Location getSpawnPoint(){
        Team team = this.map.getPlayerTeam(this); //TODO: this line throws an NPE when the player logs in for the second time (over EntityPlayerMP)
        if(team == null){
            return ((NailedWorld) world).wrapped().provider.getSpawnPoint();
        }else{
            Location s = team.getSpawnPoint();
            if(s == null){
                return ((NailedWorld) world).wrapped().provider.getSpawnPoint();
            }else{
                return s;
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("name", name)
                .add("isOnline", isOnline)
                .add("gameMode", this.getGameMode())
                .add("eid", this.entity == null ? -1 : this.entity.getEntityId())
                .toString();
    }
}
