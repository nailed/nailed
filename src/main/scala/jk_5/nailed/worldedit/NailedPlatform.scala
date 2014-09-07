package jk_5.nailed.worldedit

import java.util

import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.extension.platform.{Actor, Capability, MultiUserPlatform, Preference}
import com.sk89q.worldedit.util.command.Dispatcher
import com.sk89q.worldedit.world.World
import jk_5.nailed.api.Server
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.entity.EntityList
import net.minecraft.item.Item

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object NailedPlatform extends MultiUserPlatform {

  private var hookingEvents = false
  private def plugin = NailedWorldEditPlugin.instance

  private[worldedit] def isHookingEvents = hookingEvents

  override def resolveItem(name: String): Int = {
    if(name == null) return 0
    var i = Item.itemRegistry.getObject(name).asInstanceOf[Item]
    if(i == null){
      i = Item.itemRegistry.getObject("minecraft:" + name).asInstanceOf[Item]
    }
    if(i == null) return 0
    Item.getIdFromItem(i)
  }

  override def isValidMobType(typ: String) = EntityList.stringToClassMapping.containsKey(typ)

  override def reload(){}
  override def schedule(delay: Long, period: Long, task: Runnable) = -1

  override def getWorlds: util.List[_ <: World] = {
    val worlds = NailedDimensionManager.getVanillaWorlds
    val ret = new util.ArrayList[World](worlds.length)
    for(world <- worlds){
      ret.add(WorldEditWorld.getWorld(world))
    }
    ret
  }

  override def matchPlayer(player: Player): Player = player match {
    case p: WorldEditPlayer => p
    case p =>
      val p = getPlayerForUsername(player.getName)
      if(p == null) null else new WorldEditPlayer(p)
  }

  override def matchWorld(world: World): World = world match {
    case w: WorldEditWorld => w
    case w =>
      for(ws <- NailedDimensionManager.getVanillaWorlds){
        if(ws.getWorldInfo.getWorldName == world.getName){
          return WorldEditWorld.getWorld(ws)
        }
      }
      null
  }

  override def registerCommands(dispatcher: Dispatcher){
    for(mapping <- dispatcher.getCommands){
      plugin.getPluginManager.registerCommand(plugin, new WorldEditCommand(mapping))
    }
  }

  override def registerGameHooks() = hookingEvents = true
  override def getConfiguration = WorldEditConfig
  override def getVersion = plugin.getDescription.getVersion
  override def getPlatformName = "Nailed"
  override def getPlatformVersion = plugin.getDescription.getVersion

  override def getCapabilities: util.Map[Capability, Preference] = {
    val capabilities = new util.EnumMap[Capability, Preference](classOf[Capability])
    capabilities.put(Capability.CONFIGURATION, Preference.PREFERRED)
    capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL)
    capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL)
    capabilities.put(Capability.PERMISSIONS, Preference.NORMAL)
    capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL)
    capabilities.put(Capability.WORLD_EDITING, Preference.NORMAL)
    capabilities
  }

  override def getConnectedUsers: util.Collection[Actor] = {
    val users = new util.ArrayList[Actor]
    for(p <- Server.getInstance.getOnlinePlayers){
      users.add(WorldEditPlayer.wrap(p))
    }
    users
  }

  private def getPlayerForUsername(username: String): jk_5.nailed.api.player.Player = {
    val entity = Server.getInstance.getPlayerByName(username)
    if(entity.isEmpty) null else entity.get
  }
}
