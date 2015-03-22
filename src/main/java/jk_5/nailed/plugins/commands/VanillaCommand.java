package jk_5.nailed.plugins.commands;

import jk_5.nailed.api.command.CommandCallable;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.Description;
import jk_5.nailed.api.command.SettableDescription;
import jk_5.nailed.api.command.context.CommandLocals;
import jk_5.nailed.api.command.sender.AnalogContext;
import jk_5.nailed.api.command.util.auth.AuthorizationException;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

public class VanillaCommand implements CommandCallable {

    private static final Logger logger = LogManager.getLogger();
    private final CommandBase wrapped;
    private final Description description = new SettableDescription();

    public VanillaCommand(CommandBase wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException, AuthorizationException {
        String[] split = arguments.split(" ", -1);

        AnalogContext analog = locals.get(AnalogContext.class);
        int usernameIndex = getUsernameIndex(split);
        ICommandSender sender = locals.get(ICommandSender.class);
        String input = (String) locals.get("CMD_INPUT");
        int value = 0;

        if(usernameIndex > -1){ //Has username index
            String selector = split[usernameIndex];
            List selected = PlayerSelector.matchEntities(sender, selector, Entity.class);
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, selected.size());
            Iterator it = selected.iterator();
            while(it.hasNext()){
                Entity entity = (Entity) it.next();
                split[usernameIndex] = entity.getUniqueID().toString();
                if(execute(sender, split, input)){
                    value += 1;
                }
            }
        }else{ //No username index
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
            if(execute(sender, split, input)){
                value += 1;
            }
        }
        if(analog != null){
            analog.setPower(value);
        }
        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, value);
        return true;
    }

    private boolean execute(ICommandSender sender, String[] args, String input){
        try{
            wrapped.processCommand(sender, args);
            return true;
        }catch(WrongUsageException e) {
            IChatComponent comp = new ChatComponentTranslation("commands.generic.usage", new ChatComponentTranslation(e.getMessage()), e.getErrorObjects());
            comp.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(comp);
        }catch(net.minecraft.command.CommandException e) {
            IChatComponent comp = new ChatComponentTranslation(e.getMessage(), e.getErrorObjects());
            comp.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(comp);
        }catch(Throwable t){
            IChatComponent comp = new ChatComponentTranslation("commands.generic.exception");
            comp.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(comp);
            logger.error("Couldn\'t process command: \'" + input + "\'", t);
        }
        return false;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return wrapped.canCommandSenderUseCommand(locals.get(ICommandSender.class));
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return (List<String>) wrapped.addTabCompletionOptions(locals.get(ICommandSender.class), arguments.split(" ", -1), BlockPos.ORIGIN);
    }

    private int getUsernameIndex(String[] args){
        for(int i = 0; i < args.length; i++){
            if(wrapped.isUsernameIndex(args, i) && PlayerSelector.matchesMultiplePlayers(args[i])){
                return i;
            }
        }
        return -1;
    }
}
