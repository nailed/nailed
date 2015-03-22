package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.Tutorial;
import jk_5.nailed.api.mappack.metadata.TutorialStage;
import org.jdom2.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class XmlTutorial implements Tutorial {

    private final TutorialStage[] stages;

    public XmlTutorial(Element element) throws MappackConfigurationException {
        if(element.getChild("stages", element.getNamespace()) == null){
            this.stages = new TutorialStage[0];
        }else{
            List<TutorialStage> stages = new ArrayList<TutorialStage>();
            for(Element e : element.getChild("stages", element.getNamespace()).getChildren()){
                stages.add(new XmlTutorialStage(e));
            }
            this.stages = stages.toArray(new TutorialStage[stages.size()]);
        }
    }

    @Nonnull
    @Override
    public TutorialStage[] stages() {
        return this.stages;
    }
}
