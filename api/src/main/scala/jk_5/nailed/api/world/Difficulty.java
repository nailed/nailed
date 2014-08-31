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

package jk_5.nailed.api.world;

import java.util.HashMap;
import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public enum Difficulty {
    PEACEFUL(0),
    EASY(1),
    NORMAL(2),
    HARD(3);

    private final String name;
    private final int id;

    private static final Map<Integer, Difficulty> BY_ID = new HashMap<Integer, Difficulty>();
    private static final Map<String, Difficulty> BY_NAME = new HashMap<String, Difficulty>();

    Difficulty(int id) {
        this.id = id;
        this.name = this.name().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static Difficulty byName(String name){
        return BY_NAME.get(name);
    }

    public static Difficulty byId(int id){
        return BY_ID.get(id);
    }

    static {
        for(Difficulty d : values()){
            BY_ID.put(d.id, d);
            BY_NAME.put(d.name, d);
        }
    }
}
