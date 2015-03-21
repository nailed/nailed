package jk_5.nailed.plugins.commands;

import jk_5.nailed.api.command.Command;
import jk_5.nailed.api.command.Require;
import jk_5.nailed.server.mappack.NailedMappackRegistry;

public class ReloadCommand {

    @Command(aliases = "mappacks", desc = "Reload mappacks")
    @Require("admin")
    public void reloadMappacks(){
        NailedMappackRegistry.reload();
    }
}
