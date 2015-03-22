package jk_5.nailed.server.tileentity;

import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.stat.Stat;
import jk_5.nailed.api.map.stat.StatBlock;
import jk_5.nailed.api.map.stat.StatListener;
import jk_5.nailed.server.world.NailedDimensionManager;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityStatEmitter extends TileEntityCommandBlock implements StatBlock, StatListener, IUpdatePlayerListBox {

    public String content = "";
    public int tick = 0;
    public Stat subscribed = null;
    public Map map = null;
    public boolean register = false;
    public String response = "Enter a stat id in the box above";
    private int strength = 0;

    @Override
    public void update() {
        if(register && this.getWorld() != null && map != null){
            subscribed = map.getStatManager().getStat(content);
            if(subscribed != null){
                subscribed.addListener(TileEntityStatEmitter.this);
                response = "Stat registered. Stat emitter ready to emit";
            }else{
                response = "Could not register. Stat does not exist";
            }
            register = false;
        }
    }

    @Override
    public void setWorldObj(World world) {
        super.setWorldObj(world);
        map = NailedDimensionManager.instance().getWorld(world.provider.getDimensionId()).getMap();
    }

    //BlockCommandBlock is hardcoded to rely on CommandBlockLogic, and i don't want to change that
    //Because of that, we create a CommandBlockLogic object that intercepts all those calls for our own purpose
    private final CommandBlockLogic commandBlockLogic = new CommandBlockLogic() {

        @Override
        public void trigger(World world){

        }

        @Override
        public int getSuccessCount(){
            return strength;
        }

        @Override
        public void setCommand(String data) {
            content = data;
            if(subscribed != null) subscribed.removeListener(TileEntityStatEmitter.this);
            if(map != null){
                subscribed = map.getStatManager().getStat(content);
                if(subscribed != null){
                    subscribed.addListener(TileEntityStatEmitter.this);
                    response = "Stat registered. Stat emitter ready to emit";
                }else{
                    response = "Could not register. Stat does not exist";
                }
            }else{
                register = true;
            }
        }

        //Called when a update should be send to the client
        @Override
        public void func_145756_e() {
            getWorld().markBlockForUpdate(TileEntityStatEmitter.this.pos);
        }

        @Override
        public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
            return true;
        }

        @Override
        public String getCustomName() {
            return "StatEmitter";
        }

        @Override
        public World getEntityWorld() {
            return getWorld();
        }

        @Override
        public BlockPos getPosition() {
            return pos;
        }

        @Override
        public Vec3 getPositionVector() {
            return new Vec3(getPosition().getX() + 0.5, getPosition().getY() + 0.5, getPosition().getZ() + 0.5);
        }

        @Override
        public Entity getCommandSenderEntity() {
            return null;
        }

        @Override
        public void addChatMessage(IChatComponent component) {

        }
    };

    @Override
    public CommandBlockLogic getCommandBlockLogic() {
        return this.commandBlockLogic;
    }

    @Override
    public void setSignalStrength(int strength) {
        this.strength = strength;
        this.worldObj.updateComparatorOutputLevel(getPos(), Blocks.command_block);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("Content", this.content);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.commandBlockLogic.setCommand(tag.getString("Content"));
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        super.writeToNBT(tag);
        tag.setString("Command", content);
        tag.setInteger("SuccessCount", 0);
        tag.setString("CustomName", "Stat Emitter");
        tag.setString("LastOutput", "{\"text\":\"" + response + "\"}");
        tag.setBoolean("TrackOutput", true);
        return new S35PacketUpdateTileEntity(this.getPos(), 2, tag);
    }

    public void scheduleBlockUpdate(){
        this.commandBlockLogic.func_145756_e();
    }

    @Override
    public void onEnable() {
        setSignalStrength(15);
    }

    @Override
    public void onDisable() {
        setSignalStrength(0);
    }
}
