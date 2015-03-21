package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.gamerule.GameRule;
import jk_5.nailed.api.gamerule.GameRules;
import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.MappackWorld;
import jk_5.nailed.api.mappack.metadata.impl.DefaultMappackWorld;
import jk_5.nailed.api.util.Checks;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.Dimension;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import javax.annotation.Nonnull;

public class XmlMappackWorld implements MappackWorld {

    private final String name;
    private final String generator;
    private final Dimension dimension;
    private final Location spawnPoint;
    private final GameRules gameRules;
    private final String resourcePackUrl;
    private final Difficulty difficulty;
    private final boolean disableFood;
    private final boolean disableDamage;
    private final boolean disableBlockBreaking;
    private final boolean disableBlockPlacement;
    private final boolean isDefault;

    public static Location readLocation(Element e){
        try{
            double x = (e.getAttribute("x") != null) ? e.getAttribute("x").getDoubleValue() : 0;
            double y = (e.getAttribute("y") != null) ? e.getAttribute("y").getDoubleValue() : 64;
            double z = (e.getAttribute("z") != null) ? e.getAttribute("z").getDoubleValue() : 0;
            float yaw = (e.getAttribute("yaw") != null) ? e.getAttribute("yaw").getFloatValue() : 0;
            float pitch = (e.getAttribute("pitch") != null) ? e.getAttribute("pitch").getFloatValue() : 0;
            return new Location(x, y, z, yaw, pitch);
        }catch(DataConversionException ex){
            return new Location(0, 64, 0, 0, 0);
        }
    }

    public XmlMappackWorld(String name, Element element) throws MappackConfigurationException {
        this(name, element, DefaultMappackWorld.INSTANCE);
    }

    public XmlMappackWorld(String name, Element element, MappackWorld parent) throws MappackConfigurationException {
        Checks.notNull(parent, "parent may not be null");

        this.name = name;
        this.generator = XmlUtils.getText(element, "generator", parent.generator());

        String dim = XmlUtils.getText(element, "generator");
        if(dim == null) {
            this.dimension = parent.dimension();
        }else if(dim.equals("nether")){
            this.dimension = Dimension.NETHER;
        }else if(dim.equals("overworld")){
            this.dimension = Dimension.OVERWORLD;
        }else if(dim.equals("end")){
            this.dimension = Dimension.END;
        }else{
            throw new MappackConfigurationException("Unknown world dimension " + dim);
        }

        if(element.getChild("spawnpoint", element.getNamespace()) == null){
            this.spawnPoint = parent.spawnPoint();
        }else{
            this.spawnPoint = XmlMappackWorld.readLocation(element.getChild("spawnpoint", element.getNamespace()));
        }

        Element ruleElement;
        if(element.getChild("gamerules", element.getNamespace()) != null){
            ruleElement = element.getChild("gamerules", element.getNamespace());
        }else{
            ruleElement = new Element("dummy", element.getNamespace());
        }
        this.gameRules = new ImmutableXmlGameRules(ruleElement, (GameRules<GameRule<?>>) parent.gameRules());

        this.resourcePackUrl = XmlUtils.getText(element, "resourcepack", parent.resourcePackUrl());

        if(element.getChild("difficulty", element.getNamespace()) != null){
            this.difficulty = Difficulty.byName(element.getChild("difficulty", element.getNamespace()).getText());
        }else{
            this.difficulty = parent.difficulty();
        }

        this.disableFood = XmlUtils.getBoolean(element, "disableFood", parent.disableFood());
        this.disableDamage = XmlUtils.getBoolean(element, "disableDamage", parent.disableDamage());
        this.disableBlockBreaking = XmlUtils.getBoolean(element, "disableBlockBreaking", parent.disableBlockBreaking());
        this.disableBlockPlacement = XmlUtils.getBoolean(element, "disableBlockPlacement", parent.disableBlockPlacement());
        this.isDefault = element.getAttributeValue("default", element.getNamespace(), "false").equals("true");
    }

    @Nonnull
    @Override
    public String name() {
        return this.name;
    }

    @Nonnull
    @Override
    public String generator() {
        return this.generator;
    }

    @Nonnull
    @Override
    public Dimension dimension() {
        return this.dimension;
    }

    @Nonnull
    @Override
    public Location spawnPoint() {
        return this.spawnPoint;
    }

    @Nonnull
    @Override
    public GameRules gameRules() {
        return this.gameRules;
    }

    @Nonnull
    @Override
    public String resourcePackUrl() {
        return this.resourcePackUrl;
    }

    @Nonnull
    @Override
    public Difficulty difficulty() {
        return this.difficulty;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public boolean disableFood() {
        return this.disableFood;
    }

    @Override
    public boolean disableDamage() {
        return this.disableDamage;
    }

    @Override
    public boolean disableBlockBreaking() {
        return this.disableBlockBreaking;
    }

    @Override
    public boolean disableBlockPlacement() {
        return this.disableBlockPlacement;
    }
}
