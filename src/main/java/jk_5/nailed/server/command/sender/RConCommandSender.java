package jk_5.nailed.server.command.sender;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.command.sender.CommandSender;

public class RConCommandSender implements CommandSender {

    @Override
    public String getName() {
        return "Rcon";
    }

    @Override
    public void sendMessage(BaseComponent... component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseComponent getDescriptionComponent() {
        throw new UnsupportedOperationException();
    }
}
