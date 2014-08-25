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

package jk_5.nailed.api.chat;

public final class ClickEvent {

    /**
     * The type of action to preform on click
     */
    private final Action action;
    /**
     * Depends on action
     *
     * @see Action
     */
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public enum Action {

        /**
         * Open a url at the path given by
         * {@link ClickEvent#value}
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link ClickEvent#value}
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link ClickEvent#value}
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link ClickEvent#value} into the players text box
         */
        SUGGEST_COMMAND
    }
}
