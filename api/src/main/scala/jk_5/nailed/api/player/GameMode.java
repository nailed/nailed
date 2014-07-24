package jk_5.nailed.api.player;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * No description given
 *
 * @author jk-5
 */
public enum GameMode {
    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(2);

    private final int id;
    private static final Map<Integer, GameMode> BY_ID = Maps.newHashMap();

    GameMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    static {
        for(GameMode gm : values()){
            BY_ID.put(gm.getId(), gm);
        }
    }
}
