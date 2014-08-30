/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

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

    public static GameMode byId(int id){
        return BY_ID.get(id);
    }

    static {
        for(GameMode gm : values()){
            BY_ID.put(gm.getId(), gm);
        }
    }
}
