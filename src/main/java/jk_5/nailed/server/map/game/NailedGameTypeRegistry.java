package jk_5.nailed.server.map.game;

import com.google.common.collect.ImmutableList;
import jk_5.nailed.api.map.GameType;
import jk_5.nailed.api.map.GameTypeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NailedGameTypeRegistry implements GameTypeRegistry {

    private static final NailedGameTypeRegistry INSTANCE = new NailedGameTypeRegistry();

    private final List<GameType> types = new ArrayList<GameType>();
    private final Map<String, GameType> typesByName = new HashMap<String, GameType>();

    @Override
    public void registerGameType(@Nonnull GameType gameType) {
        types.add(gameType);
        typesByName.put(gameType.getName(), gameType);
    }

    @Override
    public void unregisterGameType(@Nonnull GameType gameType) {
        types.remove(gameType);
        typesByName.remove(gameType.getName());
    }

    @Nonnull
    @Override
    public Collection<GameType> getTypes() {
        return ImmutableList.copyOf(types);
    }

    @Nullable
    @Override
    public GameType getByName(String name) {
        return typesByName.get(name);
    }

    public static NailedGameTypeRegistry instance(){
        return INSTANCE;
    }
}
