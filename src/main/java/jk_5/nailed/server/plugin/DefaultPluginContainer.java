package jk_5.nailed.server.plugin;

import com.google.common.base.MoreObjects;
import jk_5.eventbus.Event;
import jk_5.eventbus.EventBus;
import jk_5.nailed.api.plugin.Plugin;
import jk_5.nailed.api.plugin.PluginContainer;
import jk_5.nailed.api.plugin.PluginIdentifier;
import jk_5.nailed.server.NailedPlatform;

import java.io.File;

public class DefaultPluginContainer implements PluginContainer, PluginIdentifier {

    private final Plugin annotation;
    private final Object instance;
    private final File location;
    private final EventBus eventBus = new EventBus();

    public DefaultPluginContainer(Plugin annotation, Object instance, File location) {
        this.annotation = annotation;
        this.instance = instance;
        this.location = location;

        eventBus.register(instance);
        NailedPlatform.instance().getEventBus().register(instance);
    }

    public <T extends Event> T fireEvent(T event){
        eventBus.post(event);
        return event;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public String getId() {
        return annotation.id();
    }

    @Override
    public String getName() {
        return annotation.name();
    }

    @Override
    public String getVersion() {
        return annotation.version();
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    @Override
    public PluginIdentifier getIdentifier() {
        return this;
    }

    public boolean hasLocation(){
        return this.location != null;
    }

    public File getLocation(){
        return location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("version", getVersion())
                .add("instance", instance)
                .toString();
    }
}
