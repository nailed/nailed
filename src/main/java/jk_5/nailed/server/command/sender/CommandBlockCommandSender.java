package jk_5.nailed.server.command.sender;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.command.sender.AnalogCommandSender;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.sender.WorldCommandSender;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.chat.ChatComponentConverter;
import net.minecraft.command.server.CommandBlockLogic;

public class CommandBlockCommandSender implements CommandSender, AnalogCommandSender, WorldCommandSender {

    private final CommandBlockLogic wrapped;

    public CommandBlockCommandSender(CommandBlockLogic wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public World getWorld() {
        return NailedPlatform.getWorld(wrapped.getEntityWorld().provider.getDimensionId());
    }

    @Override
    public Map getMap() {
        return getWorld().getMap();
    }

    @Override
    public String getName() {
        return wrapped.getCustomName();
    }

    @Override
    public void sendMessage(BaseComponent... component) {
        wrapped.addChatMessage(ChatComponentConverter.arrayToVanilla(component));
    }

    @Override
    public BaseComponent getDescriptionComponent() {
        return new TextComponent(this.getName());
    }
}
