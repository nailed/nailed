package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.TutorialStage;
import jk_5.nailed.api.util.Location;
import org.jdom2.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class XmlTutorialStage implements TutorialStage {

    private final String title;
    private final String[] messages;
    private final Location teleport;

    public XmlTutorialStage(Element element) throws MappackConfigurationException {
        this.title = XmlUtils.getRequiredText(element, "title");

        if(element.getChild("messages", element.getNamespace()) == null){
            this.messages = new String[0];
        }else{
            List<String> messages = new ArrayList<String>();
            for(Element e : element.getChild("messages", element.getNamespace()).getChildren()){
                messages.add(e.getText());
            }
            this.messages = messages.toArray(new String[messages.size()]);
        }

        if(element.getChild("teleport", element.getNamespace()) == null){
            this.teleport = null;
        }else{
            this.teleport = XmlMappackWorld.readLocation(element.getChild("teleport", element.getNamespace()));
        }
    }

    @Nonnull
    @Override
    public String title() {
        return this.title;
    }

    @Nonnull
    @Override
    public String[] messages() {
        return this.messages;
    }

    @Nullable
    @Override
    public Location teleport() {
        return this.teleport;
    }
}
