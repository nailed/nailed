package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.entity.metadata.EntityType;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;

import static com.google.common.base.Preconditions.checkNotNull;

public class WorldEditEntityType implements EntityType {

    private final Entity entity;

    public WorldEditEntityType(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof EntityPlayer;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof EntityEnderEye || entity instanceof IProjectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof EntityItem;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof EntityFallingBlock;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof EntityPainting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof EntityItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof EntityBoat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof EntityMinecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof EntityTNTPrimed;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof EntityXPOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof IAnimals;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof EntityAmbientCreature;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof INpc || entity instanceof IMerchant;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof EntityGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof EntityLiving && ((EntityLiving) entity).hasCustomName();
    }

    //@Override //TODO
    public boolean isArmorStand() {
        return entity instanceof EntityArmorStand;
    }
}