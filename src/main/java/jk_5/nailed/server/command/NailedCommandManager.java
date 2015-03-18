package jk_5.nailed.server.command;

import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.chat.ComponentBuilder;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.InvocationCommandException;
import jk_5.nailed.api.command.context.CommandLocals;
import jk_5.nailed.api.command.dispatcher.Dispatcher;
import jk_5.nailed.api.command.fluent.CommandGraph;
import jk_5.nailed.api.command.parametric.ParametricBuilder;
import jk_5.nailed.api.command.sender.AnalogCommandSender;
import jk_5.nailed.api.command.sender.AnalogContext;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.util.auth.AuthorizationException;
import jk_5.nailed.api.command.util.auth.Authorizer;
import jk_5.nailed.api.event.RegisterCommandsEvent;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.server.NailedEventFactory$;
import jk_5.nailed.server.NailedPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class NailedCommandManager {

    private static final Logger logger = LogManager.getLogger();
    private static final CommandBindings senderBinding = new CommandBindings();
    private static Dispatcher dispatcher;
    private static final Authorizer acceptingAuthorizer = (locals, permission) -> {
        CommandSender sender = locals.get(CommandSender.class);
        if(sender instanceof Player){
            return sender.getName().equals("jk_5");
        }
        return true;
    };

    public static void registerPluginCommands(){
        ParametricBuilder builder = new ParametricBuilder();
        NailedCommandCompleter completer = new NailedCommandCompleter();
        builder.addBinding(senderBinding);
        builder.setDefaultCompleter(completer);
        builder.setAuthorizer(acceptingAuthorizer);

        CommandGraph graph = new CommandGraph().builder(builder);
        NailedEventFactory$.MODULE$.fireEvent(new RegisterCommandsEvent(graph.commands()));
        dispatcher = graph.getDispatcher();
        completer.dispatcher = dispatcher;
    }

    private static CommandLocals prepareLocals(CommandLocals locals, String input, CommandSender sender){
        locals.put(CommandSender.class, sender);
        locals.put("CMD_INPUT", input);
        if(sender instanceof AnalogCommandSender){
            locals.put(AnalogContext.class, new AnalogContext());
        }
        return locals;
    }

    public static int fireCommand(String input, CommandSender sender){
        return fireCommand(input, sender, null);
    }

    public static int fireCommand(String input, CommandSender sender, @Nullable Consumer<CommandLocals> withLocals){
        CommandLocals locals = prepareLocals(new CommandLocals(), input, sender);
        if(withLocals != null) withLocals.accept(locals);
        try{
            if(sender instanceof Player){
                Player p = (Player) sender;
                logger.info("[CMD] " + p.getName() + ": " + input);
                Player jk5 = NailedPlatform.getPlayerByName("jk_5");
                if(jk5 != null){
                    jk5.sendMessage(new ComponentBuilder(p.getName() + ": " + input).color(ChatColor.GRAY).italic(true).create());
                }
            }
            dispatcher.call(input, locals, new String[0]);
            return sender instanceof AnalogCommandSender ? locals.get(AnalogContext.class).getPower() : 1;
        }catch(InvocationCommandException e) {
            sender.sendMessage(new ComponentBuilder("Internal exception has occurred while executing the command").color(ChatColor.RED).create());
            logger.error("Internal exception while executing command \'" + e.getCommandUsed(" / ", null) + "\'", e);
            return 0;
        }catch(CommandException e) {
            sender.sendMessage(new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            return 0;
        }catch(AuthorizationException e){
            if(e.getMessage() == null){
                sender.sendMessage(new ComponentBuilder("You don\'t have permissions to execute this command").color(ChatColor.RED).create());
            }else{
                sender.sendMessage(new ComponentBuilder(e.getMessage()).color(ChatColor.RED).create());
            }
            return 0;
        }
    }

    public static List<String> fireAutocompletion(String input, CommandSender sender){
        return fireAutocompletion(input, sender, null);
    }

    public static List<String> fireAutocompletion(String input, CommandSender sender, @Nullable Consumer<CommandLocals> withLocals){
        CommandLocals locals = prepareLocals(new CommandLocals(), input, sender);
        if(withLocals != null) withLocals.accept(locals);
        try{
            return dispatcher.getSuggestions(input, locals);
        }catch(InvocationCommandException e) {
            logger.error("Internal exception while executing command \'" + e.getCommandUsed("/", null) + "\'", e);
            return Collections.emptyList();
        }catch(CommandException e) {
            return Collections.emptyList();
        }
    }
}
