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

package jk_5.nailed.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public enum Potion {
    SPEED(1, false, false),
    SLOWNESS(2, true, false),
    HASTE(3, false, false),
    MINING_FATIGUE(4, true, false),
    STRENGTH(5, false, false),
    INSTANT_HEALTH(6, false, true),
    HARMING(7, true, true),
    JUMP_BOOST(8, false, false),
    CONFUSION(9, true, false),
    REGENERATION(10, false, false),
    RESISTANCE(11, false, false),
    FIRE_RESISTANCE(12, false, false),
    WATER_BREATHING(13, false, false),
    INVISIBILITY(14, false, false),
    BLINDNESS(15, true, false),
    NIGHT_VISION(16, false, false),
    HUNGER(17, true, false),
    WEAKNESS(18, true, false),
    POISON(19, true, false),
    WITHER(20, true, false),
    HEALTH_BOOST(21, false, false),
    ABSORPTION(22, false, false),
    SATURATION(23, false, false);

    private static final Map<Integer, Potion> BY_ID = new HashMap<Integer, Potion>();
    private static final Map<String, Potion> BY_NAME = new HashMap<String, Potion>();
    private static final List<String> NAMES = new ArrayList<String>();
    private final int id;
    private final boolean negative;
    private final boolean instant;
    private final String name;

    Potion(int id, boolean negative, boolean instant) {
        this.id = id;
        this.negative = negative;
        this.instant = instant;
        this.name = name().toLowerCase().replace('_', '-');
    }

    public int getId() {
        return id;
    }

    public boolean isNegative() {
        return negative;
    }

    public boolean isInstant() {
        return instant;
    }

    public String getName() {
        return name;
    }

    public static Potion byId(int id){
        return BY_ID.get(id);
    }

    public static Potion byName(String name){
        return BY_NAME.get(name.toLowerCase());
    }

    public static List<String> getNames() {
        return NAMES;
    }

    static{
        for(Potion potion : values()){
            BY_ID.put(potion.id, potion);
            BY_NAME.put(potion.name, potion);
            NAMES.add(potion.name);
        }
    }
}
