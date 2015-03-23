package jk_5.nailed.server.mixin.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.ISaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class TestMinecraftServerMixin {

    @Shadow
    private ISaveFormat anvilConverterForAnvilFile;

    public void nailedMethod(String msg){
        System.out.println("Fuck yeah! " + msg);
        System.out.println(anvilConverterForAnvilFile);
    }
}
