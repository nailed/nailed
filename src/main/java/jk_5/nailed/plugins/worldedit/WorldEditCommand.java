package jk_5.nailed.plugins.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.util.command.CommandMapping;
import jk_5.nailed.api.command.CommandCallable;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.Description;
import jk_5.nailed.api.command.Parameter;
import jk_5.nailed.api.command.context.CommandLocals;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.util.auth.AuthorizationException;
import jk_5.nailed.server.player.NailedPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WorldEditCommand implements CommandCallable {

    private final CommandMapping mapping;

    public WorldEditCommand(CommandMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public boolean call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException, AuthorizationException {
        CommandSender sender = locals.get(CommandSender.class);
        if((sender instanceof NailedPlayer)){
            NailedPlayer player = ((NailedPlayer) sender);
            CommandEvent weEvent = new CommandEvent(WorldEditPlayer.wrap(player.getEntity()), parentCommands[0] + " " + arguments);
            WorldEdit.getInstance().getEventBus().post(weEvent);
        }
        return false;
    }

    @Override
    public Description getDescription() {
        return new Description() {
            @Override
            public List<Parameter> getParameters() {
                List<Parameter> ret = new ArrayList<Parameter>();
                for (final com.sk89q.worldedit.util.command.Parameter p : mapping.getDescription().getParameters()) {
                    ret.add(new Parameter() {
                        @Override
                        public String getName() {
                            return p.getName();
                        }

                        @Override
                        public Character getFlag() {
                            return p.getFlag();
                        }

                        @Override
                        public boolean isValueFlag() {
                            return p.isValueFlag();
                        }

                        @Override
                        public boolean isOptional() {
                            return p.isOptional();
                        }

                        @Override
                        public String[] getDefaultValue() {
                            return p.getDefaultValue();
                        }
                    });
                }
                return ret;
            }

            @Nullable
            @Override
            public String getShortDescription() {
                return mapping.getDescription().getShortDescription();
            }

            @Nullable
            @Override
            public String getHelp() {
                return mapping.getDescription().getHelp();
            }

            @Override
            public String getUsage() {
                return mapping.getDescription().getUsage();
            }

            @Override
            public List<String> getPermissions() {
                return mapping.getDescription().getPermissions();
            }
        };
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return true; //TODO
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        CommandSender sender = locals.get(CommandSender.class);
        if((sender instanceof NailedPlayer)){
            NailedPlayer player = ((NailedPlayer) sender);
            String input = mapping.getPrimaryAlias() + " " + arguments;
            CommandSuggestionEvent event = new CommandSuggestionEvent(WorldEditPlayer.wrap(player.getEntity()), input);
            WorldEdit.getInstance().getEventBus().post(event);
            return event.getSuggestions();
        }
        return null;
    }
}
