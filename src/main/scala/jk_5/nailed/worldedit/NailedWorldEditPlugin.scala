package jk_5.nailed.worldedit

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.event.platform.{BlockInteractEvent, Interaction, PlatformReadyEvent}
import com.sk89q.worldedit.util.Location
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.{InteractAction, PlayerInteractEvent}
import jk_5.nailed.api.plugin.Plugin
import jk_5.nailed.api.plugin.logging.Logger
import jk_5.nailed.server.player.NailedPlayer

/**
 * No description given
 *
 * @author jk-5
 */
object NailedWorldEditPlugin {
  var logger: Logger = _
  var instance: NailedWorldEditPlugin = _
}

class NailedWorldEditPlugin extends Plugin {

  override def onEnable(){
    this.getDataFolder.mkdir()
    NailedWorldEditPlugin.instance = this
    NailedWorldEditPlugin.logger = this.getLogger

    this.getServer.getPluginManager.registerListener(this, this)
    this.getServer.getMessenger.registerOutgoingPluginChannel(this, "WECUI")
    this.getServer.getMessenger.registerIncomingPluginChannel(this, "WECUI", WorldEditCUIPacketHandler)

    WorldEditConfig.load()

    WorldEditBiomeRegistry.populate()

    val we = WorldEdit.getInstance()
    we.getPlatformManager.register(NailedPlatform)

    WorldEdit.getInstance.getEventBus.post(new PlatformReadyEvent)
  }

  override def onDisable(){
    WorldEdit.getInstance.getPlatformManager.unregister(NailedPlatform)
  }

  @EventHandler
  def onInteract(event: PlayerInteractEvent){
    if(!NailedPlatform.isHookingEvents) return
    val we = WorldEdit.getInstance()
    val player = WorldEditPlayer.wrap(event.player)
    val world = WorldEditWorld.getWorld(event.player.asInstanceOf[NailedPlayer].getEntity.worldObj)
    val location = new Location(world, event.x, event.y, event.z)
    event.action match {
      case InteractAction.LEFT_CLICK_AIR =>
        if(we.handleArmSwing(player)) event.setCanceled(true)
      case InteractAction.LEFT_CLICK_BLOCK =>
        val e = new BlockInteractEvent(player, location, Interaction.HIT)
        we.getEventBus.post(e)
        if(e.isCancelled) event.setCanceled(true)
        if(we.handleArmSwing(player)) event.setCanceled(true)
      case InteractAction.RIGHT_CLICK_AIR =>
        if(we.handleRightClick(player)) event.setCanceled(true)
      case InteractAction.RIGHT_CLICK_BLOCK =>
        val e = new BlockInteractEvent(player, location, Interaction.OPEN)
        we.getEventBus.post(e)
        if(e.isCancelled) event.setCanceled(true)
        if(we.handleRightClick(player)) event.setCanceled(true)
    }
  }
}
