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
import jk_5.nailed.server.tweaker.{NailedTweaker, NailedVersion}
import jk_5.nailed.server.world.{BossBar, DimensionManagerTrait, WorldProviders}
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
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
    this.pluginsFolder.mkdir()
    this.pluginManager.discoverClasspathPlugins()
    this.pluginManager.discoverPlugins(this.pluginsFolder)
    this.pluginManager.loadPlugins()
  }

  def load(server: DedicatedServer){
    this.pluginManager.enablePlugins()
  }

  @EventHandler def onBlockPlace(event: BlockPlaceEvent){
    event.setCanceled(true)
  }

  @EventHandler def onBlockBreak(event: BlockBreakEvent){
    event.setCanceled(true)
  }
}
