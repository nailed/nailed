package jk_5.nailed.server.mixin.core;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MixinTarget.class, remap = false)
public abstract class TestMinecraftServerMixin extends MixinTarget {

    @Override
    public void something() {
        nailedMethod("hai");
        super.something();
    }

    public void nailedMethod(String msg){
        System.out.println("Fuck yeah! " + msg);
    }
}
