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

package jk_5.nailed.api.plugin.logging;

/**
 * No description given
 *
 * @author jk-5
 */
public enum Level {
    OFF(org.apache.logging.log4j.Level.OFF),
    FATAL(org.apache.logging.log4j.Level.FATAL),
    ERROR(org.apache.logging.log4j.Level.ERROR),
    WARN(org.apache.logging.log4j.Level.WARN),
    INFO(org.apache.logging.log4j.Level.INFO),
    DEBUG(org.apache.logging.log4j.Level.DEBUG),
    TRACE(org.apache.logging.log4j.Level.TRACE),
    ALL(org.apache.logging.log4j.Level.ALL);

    private final org.apache.logging.log4j.Level log4jLevel;

    Level(org.apache.logging.log4j.Level log4jLevel) {
        this.log4jLevel = log4jLevel;
    }

    org.apache.logging.log4j.Level getL4JLevel() {
        return log4jLevel;
    }
}
