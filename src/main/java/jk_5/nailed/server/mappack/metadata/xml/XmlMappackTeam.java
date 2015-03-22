package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.chat.ChatColor;
import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.MappackTeam;
import jk_5.nailed.api.scoreboard.Visibility;
import org.jdom2.Element;

public class XmlMappackTeam implements MappackTeam {

    private final String id;
    private final String name;
    private final ChatColor color;
    private final boolean friendlyFire;
    private final boolean friendlyInvisiblesInvisible;
    private final Visibility nameTagVisibility;
    private final Visibility deathMessageVisibility;

    public XmlMappackTeam(Element element) throws MappackConfigurationException {
        if(element.getChild("id", element.getNamespace()) == null){
            throw new MappackConfigurationException("Invalid team. Team doesn't have an id element");
        }
        if(element.getChild("name", element.getNamespace()) == null){
            throw new MappackConfigurationException("Invalid team. Team doesn't have a name element");
        }
        if(element.getChild("color", element.getNamespace()) == null){
            throw new MappackConfigurationException("Invalid team. Team doesn't have a color");
        }

        this.id = XmlUtils.getRequiredText(element, "id");
        this.name = XmlUtils.getRequiredText(element, "name");
        this.color = XmlUtils.getRequiredChatColor(element, "color");
        this.friendlyFire = XmlUtils.getBoolean(element, "friendlyFire", true);
        this.friendlyInvisiblesInvisible = XmlUtils.getBoolean(element, "friendlyInvisiblesInvisible", true);
        this.nameTagVisibility = XmlUtils.getVisibility(element, "nameTagVisibility", Visibility.ALWAYS);
        this.deathMessageVisibility = XmlUtils.getVisibility(element, "deathMessageVisibility", Visibility.ALWAYS);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ChatColor color() {
        return this.color;
    }

    @Override
    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    @Override
    public boolean areFriendlyInvisiblesInvisible() {
        return this.friendlyInvisiblesInvisible;
    }

    @Override
    public Visibility getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    @Override
    public Visibility getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }
}
