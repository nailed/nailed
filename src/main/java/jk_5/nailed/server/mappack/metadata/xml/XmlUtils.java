package jk_5.nailed.server.mappack.metadata.xml;

import com.google.common.base.Strings;
import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.scoreboard.Visibility;
import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class XmlUtils {

    @Nullable
    public static String getText(@Nonnull Element el, @Nonnull String name){
        return getText(el, name, null);
    }

    @Nullable
    public static String getText(@Nonnull Element el, @Nonnull String name, @Nullable String defaultValue){
        Element element = el.getChild(name, el.getNamespace());
        if(element == null){
            return defaultValue;
        }else{
            return element.getText();
        }
    }

    @Nonnull
    public static String getRequiredText(@Nonnull Element el, @Nonnull String name) throws MappackConfigurationException {
        Element element = el.getChild(name, el.getNamespace());
        if(element == null){
            throw new MappackConfigurationException("Missing required " + name + " element");
        }
        return element.getText();
    }

    @Nullable
    public static ChatColor getChatColor(@Nonnull Element el, @Nonnull String name) throws MappackConfigurationException {
        String text = getText(el, name);
        if(text == null){
            return null;
        }
        ChatColor color = ChatColor.getByName(text);
        if(color == null){
            throw new MappackConfigurationException("Invalid color " + text);
        }
        return color;
    }

    @Nonnull
    public static ChatColor getRequiredChatColor(@Nonnull Element el, @Nonnull String name) throws MappackConfigurationException {
        String text = getText(el, name);
        if(text == null){
            throw new MappackConfigurationException("Missing required " + name + " element");
        }
        ChatColor color = ChatColor.getByName(text);
        if(color == null){
            throw new MappackConfigurationException("Invalid color " + text);
        }
        return color;
    }

    public static boolean getBoolean(@Nonnull Element el, @Nonnull String name, boolean defaultValue) {
        String text = getText(el, name);
        if(text == null){
            return defaultValue;
        }
        return text.equalsIgnoreCase("true") || text.equals("1");
    }

    public static Visibility getVisibility(@Nonnull Element el, @Nonnull String name, Visibility defaultValue) throws MappackConfigurationException {
        String text = getText(el, name);
        if(text == null){
            return defaultValue;
        }
        Visibility visibility = Visibility.getByName(text);
        if(visibility == null){
            throw new MappackConfigurationException("Invalid visibility " + text);
        }
        return visibility;
    }

    public static String getAttributeValue(@Nonnull Element el, @Nonnull String name){
        return getAttributeValue(el, name, null);
    }

    public static String getAttributeValue(@Nonnull Element el, @Nonnull String name, @Nullable String defaultValue){
        for (Attribute a : el.getAttributes()) {
            if(a.getName().equals(name)){
                String value = a.getValue();
                if(Strings.isNullOrEmpty(value)){
                    return defaultValue;
                }
                return a.getValue();
            }
        }
        return defaultValue;
    }
}
