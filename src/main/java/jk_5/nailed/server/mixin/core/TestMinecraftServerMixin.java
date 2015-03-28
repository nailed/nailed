package jk_5.nailed.server.mixin.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class TestMinecraftServerMixin {

    @Shadow
    public abstract void addChatMessage(IChatComponent message);

    public void nailedMethod(String msg){
        System.out.println("Fuck yeah! " + msg);
        addChatMessage(new ChatComponentText("Fuck yeah"));
    }
}
