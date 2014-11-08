package jk_5.nailed.server.mixin.impl;

import net.minecraft.world.World;

import jk_5.nailed.server.mixin.interfaces.IWorld;
import jk_5.nailed.server.tweaker.mixin.Mixin;
import jk_5.nailed.server.tweaker.mixin.Shadow;

@Mixin(World.class)
public abstract class MixinWorld implements IWorld {

    @Shadow
    private int ambientTickCountdown;

    @Override
    public int getAmbientTickCountdown() {
        return this.ambientTickCountdown;
    }
}
