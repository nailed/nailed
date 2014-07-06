package jk_5.nailed.server.world

import java.util

import jk_5.nailed.api
import jk_5.nailed.api.world.WorldContext
import jk_5.nailed.api.{Server, world}
import jk_5.nailed.server.NailedEventFactory
import jk_5.nailed.server.map.NailedMapLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.world._
import org.apache.logging.log4j.LogManager

import scala.collection.JavaConverters._
import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedDimensionManager {

  private var defaultsRegistered = false
  private val customProviders = new util.Hashtable[Int, world.WorldProvider]()
  private val vanillaWorlds = new util.Hashtable[Int, WorldServer]()
  private val worlds = new util.Hashtable[Int, NailedWorld]()
  private val dimensions = mutable.ArrayBuffer[Int]()
  private val unloadQueue = mutable.Queue[Int]()
  private val dimensionMap = new util.BitSet(java.lang.Long.SIZE << 4)
  private val logger = LogManager.getLogger

  if(!defaultsRegistered){
    //this.registerDimension(0, NailedDefaultWorldProviders.getVoidProvider)

    this.dimensionMap.set(1)

    defaultsRegistered = true
  }

  def registerDimension(id: Int, provider: world.WorldProvider){
    if(dimensions.contains(id)){
      throw new IllegalArgumentException("Failed to register dimension for id %d, One is already registered".format(id))
    }
    customProviders.put(id, provider)
    dimensions += id
    if(id >= 0) dimensionMap.set(id)
  }

  /**
   * For unregistering a dimension when the save is changed (disconnected from a server or loaded a new save
   */
  def unregisterDimension(id: Int){
    if(!dimensions.contains(id)){
      throw new IllegalArgumentException("Failed to unregister dimension for id %d; No provider registered".format(id))
    }
    dimensions -= id
  }

  def isDimensionRegistered(dim: Int) = dimensions.contains(dim)

  def getAllDimensionIds: Array[Int] = this.vanillaWorlds.keySet().asScala.toArray

  def setWorld(id: Int, world: WorldServer){
    if(world != null){
      this.vanillaWorlds.put(id, world)
      this.worlds.put(id, new NailedWorld(world))
      MinecraftServer.getServer.worldTickTimes.put(id, new Array[Long](100))
      logger.info(s"Loading dimension $id (${world.getWorldInfo.getWorldName}) (${world.func_73046_m()})")
    }else{
      this.vanillaWorlds.remove(id)
      this.worlds.remove(id)
      MinecraftServer.getServer.worldTickTimes.remove(id)
      logger.info(s"Unloading dimension $id")
    }
    val builder = mutable.ArrayBuffer[WorldServer]()
    if(this.vanillaWorlds.get(0) != null) builder += vanillaWorlds.get(0)
    for(e <- this.vanillaWorlds.entrySet()){
      val dim = e.getKey
      if(dim < -1 || dim > 1) builder += e.getValue
    }
    MinecraftServer.getServer.worldServers = builder.toArray
  }

  def initWorld(dimension: Int, ctx: WorldContext){
    if(!this.dimensions.contains(dimension) && !this.customProviders.containsKey(dimension)) throw new IllegalArgumentException("Provider type for dimension %d does not exist!".format(dimension))
    val mcserver = MinecraftServer.getServer
    val map = NailedMapLoader.getOrCreateMap(dimension)
    val name = (if(ctx.name == null) "map_" /*+ (if(map.getMappack != null) map.getMappack.getId + "_" else "")*/ + map.getId.toString else ctx.name) + (if(ctx.subName != null) "/" + ctx.subName else "")
    val saveHandler = mcserver.getActiveAnvilConverter.getSaveLoader(name, true)
    val worldInfo = saveHandler.loadWorldInfo()

    val worldSettings = if(worldInfo == null){
      //TODO: populate this from the mappack that may or may not exist
      //Arguments: seed, gameType, enable structures, hardcore mode, worldType
      val r = new WorldSettings(0, WorldSettings.GameType.ADVENTURE, false, false, WorldType.DEFAULT)
      r.func_82750_a("") //Generator settings (for flat)
      r
    }else{
      new WorldSettings(worldInfo)
    }

    val world = new WorldServer(mcserver, saveHandler, name, dimension, worldSettings, mcserver.theProfiler)
    world.addWorldAccess(new WorldManager(mcserver, world))
    NailedEventFactory.fireWorldLoad(world)
    world.getWorldInfo.setGameType(mcserver.getGameType)
    MinecraftServer.getServer.func_147139_a(EnumDifficulty.PEACEFUL)
  }

  def getVanillaWorld(dimension: Int) = this.vanillaWorlds.get(dimension)
  def getWorld(dimension: Int) = this.worlds.get(dimension)

  def getVanillaWorlds = this.vanillaWorlds.values.toArray(new Array[WorldServer](this.vanillaWorlds.size()))
  def getWorlds = this.worlds.values.toArray(new Array[api.world.World](this.worlds.size()))

  def createProviderFor(dim: Int): WorldProvider = {
    if(this.customProviders.containsKey(dim)){
      val d = new DelegatingWorldProvider(this.customProviders.get(dim))
      d.setDimension(dim)
      d
    }else throw new RuntimeException("No WorldProvider bound for dimension %d".format(dim))
  }

  def unloadWorld(id: Int) = this.unloadQueue += id
  def unloadWorlds(times: util.Hashtable[java.lang.Integer, Array[Long]]){
    for(id <- this.unloadQueue){
      val w = this.vanillaWorlds.get(id)
      try{
        if(w != null){
          w.saveAllChunks(true, null)
        }else{
          logger.warn(s"Unexpected world unload. World $id is already unloaded! Skipping it")
        }
      }catch{
        case e: MinecraftException => logger.warn(s"Error while unloading world $id", e)
      }finally{
        if(w != null){
          NailedEventFactory.fireWorldUnload(w)
          w.flush()
          this.setWorld(id, null)
        }
      }
    }
    this.unloadQueue.clear()
  }

  def getNextFreeDimensionId: Int = {
    var next = 0
    while(true){
      next = this.dimensionMap.nextClearBit(next)
      if(dimensions.contains(next)) dimensionMap.set(next) else return next
    }
    next
  }
}

trait DimensionManagerTrait extends Server {
  override def getWorld(dimensionId: Int): world.World = NailedDimensionManager.getWorld(dimensionId)
}
