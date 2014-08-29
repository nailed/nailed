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

package jk_5.nailed.server

import java.io.File

import jk_5.eventbus.EventHandler
import jk_5.nailed.api.Server
import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.api.event.{BlockBreakEvent, BlockPlaceEvent}
import jk_5.nailed.api.plugin.PluginManager
import jk_5.nailed.server.NailedEventFactory.DummyInternalListenerPlugin
import jk_5.nailed.server.map.NailedMapLoader
import jk_5.nailed.server.mappack.MappackRegistryTrait
import jk_5.nailed.server.player.PlayerRegistry
import jk_5.nailed.server.scheduler.NailedScheduler
import jk_5.nailed.server.tileentity.TileEntityStatEmitter
import jk_5.nailed.server.tweaker.{NailedTweaker, NailedVersion}
import jk_5.nailed.server.world.{BossBar, DimensionManagerTrait, WorldProviders}
import jk_5.nailed.server.worlditems.WorldItemEventHandler
import net.minecraft.command.CommandBase
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.tileentity.TileEntity
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object NailedServer
  extends Server
  with PlayerRegistry
  with DimensionManagerTrait
  with WorldProviders
  with MappackRegistryTrait
{

  private val pluginsFolder = new File(NailedTweaker.gameDir, "plugins")
  private val pluginManager = new PluginManager(this)
  private val logger = LogManager.getLogger
  val config = Settings.load()

  Server.setInstance(this)

  override def getName = "Nailed"
  override def getVersion = NailedVersion.full
  override def getPluginsFolder = this.pluginsFolder
  override def getPluginManager = this.pluginManager
  override def getScheduler = NailedScheduler
  override def getMapLoader = NailedMapLoader
  override def getConsoleCommandSender = NailedEventFactory.serverCommandSender

  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, NailedScheduler)
  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, NailedMapLoader)
  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, BossBar)
  NailedServer.getPluginManager.registerListener(DummyInternalListenerPlugin, WorldItemEventHandler)

  TileEntity.addMapping(classOf[TileEntityStatEmitter], "Nailed:StatEmitter")

  override def broadcastMessage(message: BaseComponent){
    logger.info(message.toPlainText) //TODO: format this before jline prints it out
    MinecraftServer.getServer.getConfigurationManager.sendPacketToAllPlayers(new S02PacketChat(message))
  }

  override def broadcastMessage(message: BaseComponent*){
    val msg = new TextComponent(message: _*)
    logger.info(msg.toPlainText) //TODO: format this before jline prints it out
    MinecraftServer.getServer.getConfigurationManager.sendPacketToAllPlayers(new S02PacketChat(msg))
  }

  override def broadcastMessage(message: Array[BaseComponent]){
    val msg = new TextComponent(message: _*)
    logger.info(msg.toPlainText) //TODO: format this before jline prints it out
    MinecraftServer.getServer.getConfigurationManager.sendPacketToAllPlayers(new S02PacketChat(msg))
  }

  def register(){

  }

  def preLoad(server: DedicatedServer){
    CommandBase.setAdminCommander(null) //Don't spam my log with stupid messages

    this.pluginsFolder.mkdir()
    this.pluginManager.discoverClasspathPlugins()
    this.pluginManager.discoverPlugins(this.pluginsFolder)
    this.pluginManager.loadPlugins()
  }

  def load(server: DedicatedServer){
    this.pluginManager.enablePlugins()
  }

  @EventHandler def onBlockPlace(event: BlockPlaceEvent){
    //event.setCanceled(true)
  }

  @EventHandler def onBlockBreak(event: BlockBreakEvent){
    //event.setCanceled(true)
  }
}
