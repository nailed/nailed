package jk_5.nailed.server.world;

import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.teleport.TeleportEventExitWorld;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.entity.DataWatcher;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;

public class BossBar {

    private static final BossBar INSTANCE = new BossBar();

    private final int entityId = 1234;
    private final int typeId = 63; //63 = dragon. 64 = wither;

    private BossBar() {
    }

    public S0FPacketSpawnMob getSpawnPacket(String text, Location location){
        S0FPacketSpawnMob packet = new S0FPacketSpawnMob();
        packet.entityId = entityId;
        packet.type = typeId;
        packet.x = (int) Math.floor(location.getFloorX() * 32.0D);
        packet.y = (int) Math.floor(location.getFloorY() * 32.0D);
        packet.z = (int) Math.floor(location.getFloorZ() * 32.0D);
        packet.velocityX = 0;
        packet.velocityY = 0;
        packet.velocityZ = 0;
        packet.yaw = 0;
        packet.pitch = 0;
        packet.headPitch = 0;
        packet.field_149043_l = getWatcher(text, 200);
        return packet;
    }

    public S13PacketDestroyEntities getDestroyPacket(){
        return new S13PacketDestroyEntities(entityId);
    }

    public S1CPacketEntityMetadata getUpdatePacket(String text, int health){
        return new S1CPacketEntityMetadata(entityId, getWatcher(text, health), true);
    }

    private DataWatcher getWatcher(String text, int health){
        DataWatcher watcher = new DataWatcher(null);
        watcher.addObject(0, (byte) 0x20); //Flags. 0x20 = invisible
        watcher.addObject(6, (float) health);
        watcher.addObject(10, text); //Entity name
        watcher.addObject(11, (byte) 1); //Show name, 1 = show, 0 = don't show
        return watcher;
    }

    @EventHandler
    public void onPlayerExitWorld(TeleportEventExitWorld event){
        ((NailedPlayer) event.getPlayer()).sendPacket(this.getDestroyPacket());
    }

    public static BossBar instance() {
        return INSTANCE;
    }
}
