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

final public class HoverEvent {

    private final Action action;
    private final BaseComponent[] value;

    public HoverEvent(Action action, BaseComponent... value) {
        this.action = action;
        this.value = value;
    }

    public BaseComponent[] getValue() {
        return value;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM
    }
}
