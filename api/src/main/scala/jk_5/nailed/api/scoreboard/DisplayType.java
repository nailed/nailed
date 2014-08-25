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

package jk_5.nailed.api.scoreboard;

/**
 * Describes where an objective should be displayed on the client gui
 *
 * @author jk-5
 */
public enum DisplayType {
    /**
     * Displays the objective in the player list that is displayed when the player presses [Tab]
     */
    PLAYER_LIST(0),
    /**
     * Displays the objective in the sidebar on the right of the screen
     */
    SIDEBAR(1),
    /**
     * Displays the objective below the name of the player (Above a player's head)
     */
    BELOW_NAME(2);

    /**
     * The id of the {@link DisplayType}. This is used for vanilla compatability and networking
     */
    private final int id;

    DisplayType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
