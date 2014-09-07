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

package jk_5.nailed.plugins.internal

import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.plugins.internal.command._

/**
 * No description given
 *
 * @author jk-5
 */
class NailedInternalPlugin extends Plugin {

  override def onEnable(){
    this.getPluginManager.registerCommand(this, CommandGoto)
    this.getPluginManager.registerCommand(this, CommandTps)
    this.getPluginManager.registerCommand(this, CommandGamerule)
    this.getPluginManager.registerCommand(this, CommandLoadmap)
    this.getPluginManager.registerCommand(this, CommandTeam)
    this.getPluginManager.registerCommand(this, CommandStatemitter)
    this.getPluginManager.registerCommand(this, CommandGamemode)
    this.getPluginManager.registerCommand(this, CommandTime)
    this.getPluginManager.registerCommand(this, CommandWeather)
    this.getPluginManager.registerCommand(this, CommandToggleDownfall)
    this.getPluginManager.registerCommand(this, CommandKick)
    this.getPluginManager.registerCommand(this, CommandHelp)
    this.getPluginManager.registerCommand(this, CommandKill)
    this.getPluginManager.registerCommand(this, CommandHeal)
    this.getPluginManager.registerCommand(this, CommandEffect)
    this.getPluginManager.registerCommand(this, CommandPos)
    this.getPluginManager.registerCommand(this, CommandExperience)
    this.getPluginManager.registerCommand(this, CommandAnalog)
    this.getPluginManager.registerCommand(this, CommandDifficulty)
    this.getPluginManager.registerCommand(this, CommandStartGame)
    this.getPluginManager.registerCommand(this, new RemovedCommand("defaultgamemode"))
    this.getPluginManager.registerCommand(this, new RemovedCommand("debug"))
    this.getPluginManager.registerCommand(this, new RemovedCommand("setworldspawn"))
    this.getPluginManager.registerCommand(this, new RemovedCommand("save-all"))
    this.getPluginManager.registerCommand(this, new RemovedCommand("save-on"))
    this.getPluginManager.registerCommand(this, new RemovedCommand("save-off"))
  }
}
