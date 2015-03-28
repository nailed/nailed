package jk_5.nailed.server.chat;

import jk_5.nailed.api.chat.*;
import net.minecraft.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
public class ChatComponentConverter {

    private static final Logger logger = LogManager.getLogger();

    public static IChatComponent toVanilla(BaseComponent component){
        IChatComponent base = singleComponentToVanilla(component);
        if(component.getExtra() != null){
            for(BaseComponent child : component.getExtra()){
                base.appendSibling(toVanilla(child));
            }
        }
        return base;
    }

    public static IChatComponent arrayToVanilla(BaseComponent[] comp){
        IChatComponent base = new ChatComponentText("");
        for(BaseComponent c : comp){
            base.appendSibling(toVanilla(c));
        }
        return base;
    }

    public static IChatComponent singleComponentToVanilla(BaseComponent component){
        IChatComponent base;
        if(component instanceof TextComponent){
            base = new ChatComponentText(((TextComponent) component).getText());
        }else if(component instanceof TranslatableComponent){
            base = new ChatComponentTranslation(((TranslatableComponent) component).getTranslate(), ((TranslatableComponent) component).getWith());
        }else{
            logger.warn("Was not able to convert component {0} to vanilla", component.toString());
            base = null;
        }
        ChatStyle style = new ChatStyle();
        style.setColor(convertColor(component.getColorRaw()));
        style.setBold(component.isBoldRaw());
        style.setItalic(component.isBoldRaw());
        style.setUnderlined(component.isBoldRaw());
        style.setStrikethrough(component.isBoldRaw());
        style.setObfuscated(component.isObfuscatedRaw());
        if(component.getHoverEvent() != null){
            HoverEvent e = component.getHoverEvent();
            net.minecraft.event.HoverEvent.Action newAction;
            switch(e.getAction()){
                case SHOW_ACHIEVEMENT:
                    newAction = net.minecraft.event.HoverEvent.Action.SHOW_ACHIEVEMENT;
                    break;
                case SHOW_ITEM:
                    newAction = net.minecraft.event.HoverEvent.Action.SHOW_ITEM;
                    break;
                case SHOW_TEXT:
                    newAction = net.minecraft.event.HoverEvent.Action.SHOW_TEXT;
                    break;
                default:
                    newAction = null;
                    break;
            }
            if(newAction != null){
                style.setChatHoverEvent(new net.minecraft.event.HoverEvent(newAction, arrayToVanilla(e.getValue())));
            }
        }
        if(component.getClickEvent() != null){
            ClickEvent e = component.getClickEvent();
            net.minecraft.event.ClickEvent.Action newAction;
            switch(e.getAction()){
                case OPEN_FILE:
                    newAction = net.minecraft.event.ClickEvent.Action.OPEN_FILE;
                    break;
                case OPEN_URL:
                    newAction = net.minecraft.event.ClickEvent.Action.OPEN_URL;
                    break;
                case RUN_COMMAND:
                    newAction = net.minecraft.event.ClickEvent.Action.RUN_COMMAND;
                    break;
                case SUGGEST_COMMAND:
                    newAction = net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND;
                    break;
                default:
                    newAction = null;
                    break;
            }
            if(newAction != null){
                style.setChatClickEvent(new net.minecraft.event.ClickEvent(newAction, e.getValue()));
            }
        }
        base.setChatStyle(style);
        return base;
    }

    public static EnumChatFormatting convertColor(ChatColor color){
        if(color == null){
            return null;
        }else{
            for(EnumChatFormatting c : EnumChatFormatting.values()){
                if(c.toString().equals("\u00a7" + color.getCode())){
                    return c;
                }
            }
            return null;
        }
    }
}
