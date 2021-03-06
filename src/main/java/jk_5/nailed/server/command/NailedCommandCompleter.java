package jk_5.nailed.server.command;

import jk_5.nailed.api.command.CommandCallable;
import jk_5.nailed.api.command.CommandException;
import jk_5.nailed.api.command.CommandMapping;
import jk_5.nailed.api.command.completion.CommandCompleter;
import jk_5.nailed.api.command.context.CommandLocals;
import jk_5.nailed.api.command.dispatcher.Dispatcher;
import jk_5.nailed.api.command.parametric.ParameterData;
import jk_5.nailed.api.command.parametric.ParametricCallable;
import jk_5.nailed.api.command.parametric.binding.BindingBehavior;
import jk_5.nailed.api.command.sender.CommandSender;
import jk_5.nailed.api.command.sender.MapCommandSender;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.Team;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.server.NailedPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NailedCommandCompleter implements CommandCompleter {

    private static final Logger logger = LogManager.getLogger();
    private static Field parameters;
    Dispatcher dispatcher;

    static {
        try{
            parameters = ParametricCallable.class.getDeclaredField("parameters");
            parameters.setAccessible(true);
        }catch(NoSuchFieldException e){
            logger.warn("Was not able to find parameters field in ParametricCallable");
            parameters = null;
        }
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        String input = (String) locals.get("CMD_INPUT");
        String inputPath = input.substring(0, input.length() - arguments.length()).trim();
        String partialInput = input.substring(input.lastIndexOf(' ') + 1);
        String path = input.substring(0, input.lastIndexOf(' '));
        List<String> out = new ArrayList<String>();
        String[] split = inputPath.split(" ", -1);
        String[] splitInput = arguments.split(" " , -1);
        CommandMapping p = dispatcher.get(inputPath);

        Dispatcher disp = dispatcher;

        ParameterData[] paramData = new ParameterData[0];
        for(int i = 0; i < split.length; i++){
            CommandCallable callable = disp.get(split[i]).getCallable();
            if(callable instanceof Dispatcher){
                disp = (Dispatcher) callable;
            }else if(callable instanceof ParametricCallable){
                try{
                    paramData = (ParameterData[]) parameters.get(callable);
                    break;
                }catch(IllegalAccessException e){
                    //I don't care
                }
            }else{
                break;
            }
        }

        List<ParameterData> realParamsBuilder = new ArrayList<ParameterData>();
        for(ParameterData pd : paramData){
            if(pd.getBinding().getBehavior(pd) != BindingBehavior.PROVIDES){
                realParamsBuilder.add(pd);
            }
        }
        ParameterData[] realParams = realParamsBuilder.toArray(new ParameterData[realParamsBuilder.size()]);
        ParameterData completingParam = realParams.length >= splitInput.length ? realParams[splitInput.length - 1] : null;

        if(completingParam == null) return out;
        String completingName = completingParam.getName();
        if(completingName.equals("gamemode")){
            out.add("survival");
            out.add("creative");
            out.add("adventure");
            out.add("spectator");
        }else if(completingName.equals("difficulty")){
            out.add("peaceful");
            out.add("easy");
            out.add("medium");
            out.add("hard");
        }else if(completingName.equals("weathertype")){
            out.add("clear");
            out.add("dry");
            out.add("rain");
            out.add("thunder");
        }else if(completingName.equals("mappack")){
            out.addAll(NailedPlatform.instance().getMappackRegistry().getAllIds());
        }else if(completingName.equals("player")){
            for(Player pl : NailedPlatform.instance().getOnlinePlayers()){
                out.add(pl.getName());
            }
        }else if(completingName.equals("team")){
            CommandSender sender = locals.get(CommandSender.class);
            if(sender instanceof MapCommandSender){
                Map map = ((MapCommandSender) sender).getMap();
                if(map != null){
                    for(Team team : map.getTeams()){
                        out.add(team.id());
                    }
                }
            }
        }else if(completingName.equals("gamewinnable")){
            CommandSender sender1 = locals.get(CommandSender.class);
            if(sender1 instanceof MapCommandSender){
                Map map = ((MapCommandSender) sender1).getMap();
                if(map != null){
                    for(Team team : map.getTeams()){
                        out.add(team.id());
                    }
                }
            }
            for(Player pl : NailedPlatform.instance().getOnlinePlayers()){
                out.add(pl.getName());
            }
        }
        List<String> ret = new ArrayList<String>();
        for(String s : out){
            if(s.toLowerCase().startsWith(partialInput.toLowerCase())){
                ret.add(s);
            }
        }
        return ret;
    }
}
