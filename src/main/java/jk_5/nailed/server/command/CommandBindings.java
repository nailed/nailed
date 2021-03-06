package jk_5.nailed.server.command;

import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.Platform;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.parametric.ParameterException;
import jk_5.nailed.api.command.parametric.argument.ArgumentStack;
import jk_5.nailed.api.command.parametric.binding.BindingBehavior;
import jk_5.nailed.api.command.parametric.binding.BindingHelper;
import jk_5.nailed.api.command.parametric.binding.BindingMatch;
import jk_5.nailed.api.command.sender.AnalogContext;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.sender.MapCommandSender;
import jk_5.nailed.api.command.sender.WorldCommandSender;
import jk_5.nailed.api.map.GameWinnable;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.WeatherType;
import jk_5.nailed.server.NailedPlatform;

public class CommandBindings extends BindingHelper {

    @BindingMatch(type = CommandSender.class, behavior = BindingBehavior.PROVIDES)
    public CommandSender getCommandSender(ArgumentStack stack){
        return stack.getContext().getLocals().get(CommandSender.class);
    }

    @BindingMatch(type = WorldCommandSender.class, behavior = BindingBehavior.PROVIDES)
    public WorldCommandSender getWorldCommandSender(ArgumentStack stack) throws CommandException {
        CommandSender sender = stack.getContext().getLocals().get(CommandSender.class);
        if(sender instanceof WorldCommandSender){
            return ((WorldCommandSender) sender);
        }else{
            throw new CommandException("You are not in a world");
        }
    }

    @BindingMatch(type = MapCommandSender.class, behavior = BindingBehavior.PROVIDES)
    public MapCommandSender getMapCommandSender(ArgumentStack stack) throws CommandException {
        CommandSender sender = stack.getContext().getLocals().get(CommandSender.class);
        if(sender instanceof MapCommandSender){
            return ((MapCommandSender) sender);
        }else{
            throw new CommandException("You are not in a map");
        }
    }

    @BindingMatch(type = Platform.class, behavior = BindingBehavior.PROVIDES)
    public Platform getPlatform(ArgumentStack stack) throws CommandException {
        return NailedPlatform.instance();
    }

    @BindingMatch(type = AnalogContext.class, behavior = BindingBehavior.PROVIDES)
    public AnalogContext getAnalogContext(ArgumentStack stack) throws CommandException {
        return stack.getContext().getLocals().get(AnalogContext.class);
    }

    @BindingMatch(type = Player.class, behavior = BindingBehavior.CONSUMES)
    public Player getPlayer(ArgumentStack stack) throws ParameterException, CommandException {
        String name = stack.next();
        Player player = NailedPlatform.instance().getPlayerByName(name);
        if(player == null){
            throw new CommandException("Player " + name + " is not online");
        }
        return player;
    }

    @BindingMatch(type = GameMode.class, behavior = BindingBehavior.CONSUMES)
    public GameMode getGameMode(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        if(input.equals("survival") || input.equals("0")){
            return GameMode.SURVIVAL;
        }else if(input.equals("creative") || input.equals("1")){
            return GameMode.CREATIVE;
        }else if(input.equals("adventure") || input.equals("2")){
            return GameMode.ADVENTURE;
        }else if(input.equals("spectator") || input.equals("3")){
            return GameMode.SPECTATOR;
        }else{
            throw new CommandException("Unknown gamemode \'" + input + "\'");
        }
    }

    @BindingMatch(type = Difficulty.class, behavior = BindingBehavior.CONSUMES)
    public Difficulty getDifficulty(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        if(input.equals("peaceful") || input.equals("0")){
            return Difficulty.PEACEFUL;
        }else if(input.equals("easy") || input.equals("1")){
            return Difficulty.EASY;
        }else if(input.equals("normal") || input.equals("2")){
            return Difficulty.NORMAL;
        }else if(input.equals("hard") || input.equals("3")){
            return Difficulty.HARD;
        }else{
            throw new CommandException("Unknown difficulty \'" + input + "\'");
        }
    }

    @BindingMatch(type = WeatherType.class, behavior = BindingBehavior.CONSUMES)
    public WeatherType getWeather(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        if(input.equals("clear") || input.equals("dry")){
            return WeatherType.DRY;
        }else if(input.equals("rain")){
            return WeatherType.RAIN;
        }else if(input.equals("thunder")){
            return WeatherType.THUNDER;
        }else{
            throw new CommandException("Unknown weather type \'" + input + "\'");
        }
    }

    @BindingMatch(type = Mappack.class, behavior = BindingBehavior.CONSUMES)
    public Mappack getMappack(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        Mappack mappack = NailedPlatform.instance().getMappackRegistry().getByName(input);
        if(mappack == null) throw new CommandException("Unknown mappack \'" + input + "\'");
        return mappack;
    }

    @BindingMatch(type = Team.class, behavior = BindingBehavior.CONSUMES)
    public Team getTeam(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        CommandSender sender = stack.getContext().getLocals().get(CommandSender.class);
        if(sender instanceof MapCommandSender){
            Team team = ((MapCommandSender) sender).getMap().getTeam(input);
            if(team == null){
                throw new CommandException("Unknown team \'" + input + "\'");
            }else{
                return team;
            }
        }else{
            throw new CommandException("You are not in a map");
        }
    }

    @BindingMatch(type = GameWinnable.class, behavior = BindingBehavior.CONSUMES)
    public GameWinnable getWinnable(ArgumentStack stack) throws ParameterException, CommandException {
        String input = stack.next();
        CommandSender sender = stack.getContext().getLocals().get(CommandSender.class);
        if(sender instanceof MapCommandSender){
            Team team = ((MapCommandSender) sender).getMap().getTeam(input);
            if(team == null){
                Player player = NailedPlatform.instance().getPlayerByName(input);
                if(player == null){
                    throw new CommandException(input + " is not a team nor a player. It can\'t win this game");
                }
                return player;
            }else{
                return team;
            }
        }else{
            throw new CommandException("You are not in a map");
        }
    }
}
