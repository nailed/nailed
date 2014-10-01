package jk_5.nailed.server

import java.io.File

import com.google.gson.Gson
import jk_5.eventbus.EventBus
import jk_5.nailed.api.Platform
import jk_5.nailed.api.chat.{BaseComponent, TextComponent}
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent
import jk_5.nailed.api.messaging.StandardMessenger
import jk_5.nailed.server.map.NailedMapLoader
import jk_5.nailed.server.map.game.NailedGameTypeRegistry
import jk_5.nailed.server.mappack.NailedMappackRegistry
import jk_5.nailed.server.player.PlayerRegistry
import jk_5.nailed.server.plugin.NailedPluginManager
import jk_5.nailed.server.scheduler.NailedScheduler
import jk_5.nailed.server.tileentity.TileEntityStatEmitter
import jk_5.nailed.server.tweaker.NailedTweaker
import jk_5.nailed.server.utils.NailedPlayerSelector
import jk_5.nailed.server.world.{BossBar, DimensionManagerTrait, WorldProviders}
import jk_5.nailed.server.worlditems.WorldItemEventHandler
import net.minecraft.command.CommandBase
import net.minecraft.launchwrapper.Launch
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
object NailedPlatform
  extends Platform
  with PlayerRegistry
  with DimensionManagerTrait
  with WorldProviders {

  val logger = LogManager.getLogger
  val config = Settings.load()
  val globalEventBus = new EventBus
  val pluginsDir = new File(NailedTweaker.gameDir, "plugins")
  val gson = new Gson

  logger.info("PLATFORM LOADED BY " + this.getClass.getClassLoader)
  if(this.getClass.getClassLoader == Launch.classLoader){
    globalEventBus.register(this)
    globalEventBus.register(NailedScheduler)
    globalEventBus.register(NailedMapLoader)
    globalEventBus.register(BossBar)
    globalEventBus.register(WorldItemEventHandler)
  }else{
    logger.info("------------------")
    logger.info("WRONG CLASSLOADER!")
    logger.info("------------------")
    Thread.dumpStack()
  }

  override val getAPIVersion = classOf[Platform].getPackage.getImplementationVersion
  override val getImplementationVersion = this.getClass.getPackage.getImplementationVersion
  override val getImplementationName = "Nailed"
  override val getRuntimeDirectory = NailedTweaker.gameDir
  override val getMessenger = new StandardMessenger(this)
  override def getGameTypeRegistry = NailedGameTypeRegistry
  override def getScheduler = NailedScheduler
  override def getPlayerSelector = NailedPlayerSelector
  override def getConsoleCommandSender = NailedEventFactory.serverCommandSender
  override def getMappackRegistry = NailedMappackRegistry

  def preLoad(server: DedicatedServer){
    CommandBase.setAdminCommander(null) //Don't spam my log with stupid messages

    TileEntity.addMapping(classOf[TileEntityStatEmitter], "Nailed:StatEmitter")

    this.pluginsDir.mkdir()
    this.getPluginManager.loadPlugins(this.pluginsDir)

    NailedEventFactory.fireEvent(new RegisterMappacksEvent(NailedMappackRegistry, NailedMapLoader))
  }

  def load(server: DedicatedServer){
    this.getPluginManager.enablePlugins()
  }

  override def getPluginManager = NailedPluginManager

  override def broadcastMessage(message: BaseComponent*){
    val msg = new TextComponent(message: _*)
    logger.info(msg.toPlainText) //TODO: format this before we print it out
    MinecraftServer.getServer.getConfigurationManager.sendPacketToAllPlayers(new S02PacketChat(msg))
  }
}
