package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.mappack.metadata.StatConfig;
import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class XmlStatConfig implements StatConfig {

    private final String name;
    private final String track;
    private final Map<String, String> attributes;

    public XmlStatConfig(String name, Element element) {
        this.name = name;

        Attribute trackAttr = element.getAttribute("track", element.getNamespace());
        if(trackAttr != null){
            this.track = trackAttr.getValue();
        }else{
            this.track = null;
        }

        this.attributes = new HashMap<String, String>();
        for (Element e : element.getChildren()) {
            this.attributes.put(e.getName(), e.getText());
        }
    }

    @Nonnull
    @Override
    public String name() {
        return this.name;
    }

    @Nonnull
    @Override
    public String track() {
        return this.track;
    }

    @Nonnull
    @Override
    public Map<String, String> attributes() {
        return this.attributes;
    }
}
