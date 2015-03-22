package jk_5.nailed.server.mappack;

import com.google.common.collect.ImmutableList;
import jk_5.nailed.api.event.mappack.MappackRegisteredEvent;
import jk_5.nailed.api.event.mappack.MappackUnregisteredEvent;
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.mappack.MappackRegistry;
import jk_5.nailed.server.NailedEventFactory;
import jk_5.nailed.server.map.NailedMapLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NailedMappackRegistry implements MappackRegistry {

    private static final NailedMappackRegistry INSTANCE = new NailedMappackRegistry();
    private static final Logger logger = LogManager.getLogger();

    private final Map<String, Mappack> mappacks = new HashMap<String, Mappack>();

    @Override
    public boolean register(@Nonnull Mappack mappack) {
        if(mappacks.containsKey(mappack.getId()) || mappacks.containsValue(mappack)){
            return false;
        }
        this.mappacks.put(mappack.getId(), mappack);
        NailedEventFactory.fireEvent(new MappackRegisteredEvent(mappack));
        return true;
    }

    @Nullable
    @Override
    public Mappack getByName(@Nonnull String name) {
        return this.mappacks.get(name);
    }

    @Nonnull
    @Override
    public <T> Collection<T> getByType(@Nonnull Class<? extends T> cl) {
        List<T> ret = new ArrayList<T>();
        for (Mappack mappack : this.mappacks.values()) {
            if(cl.isAssignableFrom(mappack.getClass())){
                ret.add((T) mappack);
            }
        }
        return ret;
    }

    @Nonnull
    @Override
    public Collection<Mappack> getAll() {
        return ImmutableList.copyOf(this.mappacks.values());
    }

    @Nonnull
    @Override
    public Collection<String> getAllIds() {
        return ImmutableList.copyOf(this.mappacks.keySet());
    }

    @Override
    public boolean unregister(@Nonnull Mappack mappack) {
        if(!mappacks.containsKey(mappack.getId()) && !mappacks.containsValue(mappack)){
            return false;
        }
        this.mappacks.remove(mappack.getId());
        NailedEventFactory.fireEvent(new MappackUnregisteredEvent(mappack));
        return true;
    }

    public void reload(){
        logger.info("Reloading mappacks");
        mappacks.clear();
        NailedEventFactory.fireEvent(new RegisterMappacksEvent(this, NailedMapLoader.instance()));
        NailedMapLoader.instance().checkLobbyMappack();
    }

    public static NailedMappackRegistry instance(){
        return INSTANCE;
    }
}
