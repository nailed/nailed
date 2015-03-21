package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.*;
import jk_5.nailed.api.mappack.metadata.impl.DefaultMappackAuthor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlMappackMetadata implements MappackMetadata {

    private static final SAXBuilder builder = new SAXBuilder();

    private final String name;
    private final String version;
    private final MappackAuthor[] authors;
    private final XmlTutorial tutorial;
    private final MappackTeam[] teams;
    private final MappackWorld[] worlds;
    private final String gameType;
    private final StatConfig[] stats;

    public static XmlMappackMetadata fromFile(File file) throws MappackConfigurationException {
        try {
            Document doc = builder.build(file);
            Element e = doc.getRootElement();
            if(e.getName().equals("game")){
                return new XmlMappackMetadata(e, e.getNamespace());
            }else{
                throw new MappackConfigurationException("game.xml does not contain the root game element");
            }
        }catch(JDOMException e){
            throw new MappackConfigurationException("game.xml syntax error", e);
        }catch(IOException e){
            throw new MappackConfigurationException("Was not able to read game.xml", e);
        }
    }

    public static XmlMappackMetadata fromResource(String path) throws MappackConfigurationException {
        try {
            Document doc = builder.build(XmlMappackMetadata.class.getResourceAsStream("/" + path));
            Element e = doc.getRootElement();
            if(e.getName().equals("game")){
                return new XmlMappackMetadata(e, e.getNamespace());
            }else{
                throw new MappackConfigurationException("game.xml does not contain the root game element");
            }
        }catch(JDOMException e){
            throw new MappackConfigurationException("game.xml syntax error", e);
        }catch(IOException e){
            throw new MappackConfigurationException("Was not able to read game.xml", e);
        }
    }

    public XmlMappackMetadata(Element element, Namespace ns) throws MappackConfigurationException {
        if(element.getChild("name", ns) == null){
            throw new MappackConfigurationException("Missing required <name> element");
        }
        if(element.getChild("version", ns) == null){
            throw new MappackConfigurationException("Missing required <version> element");
        }
        if(element.getChild("authors", ns) == null){
            throw new MappackConfigurationException("Missing required <authors> element");
        }
        if(element.getChild("worlds", ns) == null){
            throw new MappackConfigurationException("Missing required <worlds> element");
        }
        if(element.getChild("worlds", ns).getChildren().size() == 0){
            throw new MappackConfigurationException("<worlds> element should have at least 1 <world>");
        }

        this.name = element.getChild("name", ns).getText();
        this.version = element.getChild("version", ns).getText();

        List<MappackAuthor> authors = new ArrayList<MappackAuthor>();
        for (Element e : element.getChild("authors", ns).getChildren()) {
            if(!e.getName().equals("author")){
                throw new MappackConfigurationException("Invalid element in authors list: " + e.getName());
            }
            if(e.getChild("name", ns) == null){
                throw new MappackConfigurationException("Missing required element <name> in <author> element");
            }
            if(e.getChild("role", ns) == null){
                throw new MappackConfigurationException("Missing required element <role> in <author> element");
            }
            authors.add(new DefaultMappackAuthor(e.getChild("name", ns).getText(), e.getChild("role", ns).getText()));
        }

        this.authors = authors.toArray(new MappackAuthor[authors.size()]);

        if(element.getChild("tutorial", ns) == null){
            this.tutorial = null;
        }else{
            this.tutorial = new XmlTutorial(element.getChild("tutorial", ns));
        }

        if(element.getChild("teams", ns) == null){
            this.teams = new MappackTeam[0];
        }else{
            List<MappackTeam> teams = new ArrayList<MappackTeam>();
            for (Element e : element.getChild("teams", ns).getChildren()) {
                teams.add(new XmlMappackTeam(e));
            }
            this.teams = teams.toArray(new MappackTeam[teams.size()]);
        }

        List<MappackWorld> worlds = new ArrayList<MappackWorld>();
        for (Element e : element.getChild("worlds", ns).getChildren()) {
            if(e.getChild("name", ns) == null){
                throw new MappackConfigurationException("Missing required element <name> in <world> element");
            }
            worlds.add(new XmlMappackWorld(e.getChild("name", ns).getText(), e, null)); //TODO: remove null in here as it is overloaded
        }
        this.worlds = worlds.toArray(new MappackWorld[worlds.size()]);

        if(element.getChild("gametype", ns) == null){
            this.gameType = null;
        }else{
            this.gameType = element.getChild("gametype").getAttributeValue("name", ns);
        }

        if(element.getChild("stats", ns) == null){
            this.stats = new StatConfig[0];
        }else{
            List<StatConfig> stats = new ArrayList<StatConfig>();
            for (Element e : element.getChild("stats", ns).getChildren()) {
                if(!e.getName().equals("stat")){
                    throw new MappackConfigurationException("Invalid element in stats list: " + e.getName());
                }
                if(e.getAttribute("name", ns) == null){
                    throw new MappackConfigurationException("Missing required attribute 'name' in stat element");
                }
                String name = e.getAttribute("name", ns).getValue();
                if(name.isEmpty() || name.length() == 0){
                    throw new MappackConfigurationException("Missing required attribute 'name' in stat element");
                }
                stats.add(new XmlStatConfig(name, e));
            }
            this.stats = stats.toArray(new StatConfig[stats.size()]);
        }
    }

    @Nonnull
    @Override
    public String name() {
        return this.name;
    }

    @Nonnull
    @Override
    public String version() {
        return this.version;
    }

    @Nonnull
    @Override
    public MappackAuthor[] authors() {
        return this.authors;
    }

    @Nonnull
    @Override
    public MappackWorld[] worlds() {
        return this.worlds;
    }

    @Nonnull
    @Override
    public MappackTeam[] teams() {
        return this.teams;
    }

    @Nonnull
    @Override
    public Tutorial tutorial() {
        return this.tutorial;
    }

    @Nonnull
    @Override
    public String gameType() {
        return this.gameType;
    }

    @Nonnull
    @Override
    public StatConfig[] stats() {
        return this.stats;
    }
}
