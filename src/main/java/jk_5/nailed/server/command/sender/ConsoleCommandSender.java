package jk_5.nailed.server.command.sender;

import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.command.sender.CommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsoleCommandSender implements CommandSender {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String getName() {
        return "Server";
    }

    @Override
    public void sendMessage(BaseComponent... component) {
        logger.info(new TextComponent(component).toPlainText());
    }

    @Override
    public BaseComponent getDescriptionComponent() {
        return new TextComponent(this.getName());
    }
}
