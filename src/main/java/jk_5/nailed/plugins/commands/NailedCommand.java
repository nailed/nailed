package jk_5.nailed.plugins.commands;

import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.server.NailedPlatform;

public class NailedCommand {

    @Command(aliases = "version", desc = "Tells you the nailed version")
    public void reloadMappacks(CommandSender sender){
        TextComponent message = new TextComponent("Nailed version " + NailedPlatform.instance().getImplementationVersion() + " implementing api version " + NailedPlatform.instance().getAPIVersion());
        message.setColor(ChatColor.GOLD);
        sender.sendMessage(message);
    }
}
