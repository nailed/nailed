package jk_5.nailed.server.mixin.core.server;

import jk_5.nailed.server.map.NailedMap;
import jk_5.nailed.server.map.NailedMapLoader;
import jk_5.nailed.server.world.NailedWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow
    private ServerConfigurationManager serverConfigManager;

    @Shadow
    public abstract void setUserMessage(String message);

    @Shadow
    public abstract void initialWorldChunkLoad();

    @Overwrite
    protected void loadAllWorlds(String p_71247_1_, String p_71247_2_, long seed, WorldType type, String p_71247_6_) {
        this.setUserMessage("menu.loadingLevel");
        NailedMap map = NailedMapLoader.instance().createLobbyMap();
        NailedWorld world = (NailedWorld) map.worldsArray()[0];
        this.serverConfigManager.setPlayerManager(new WorldServer[]{world.getWrapped()});
        this.initialWorldChunkLoad();
    }

    @Overwrite
    public String getServerModName(){
        return "nailed";
    }
}
