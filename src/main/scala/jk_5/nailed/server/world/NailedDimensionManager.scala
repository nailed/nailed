package jk_5.nailed.server.world

import java.util

import jk_5.nailed.server.NailedEventFactory
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
  private val providers = new util.Hashtable[Int, Class[_ <: WorldProvider]]()
  private val spawnSettings = new util.Hashtable[Int, Boolean]()
  private val vanillaWorlds = new util.Hashtable[Int, WorldServer]()
  private val dimensions = new util.Hashtable[Int, Int]()
  private val unloadQueue = mutable.Queue[Int]()
  private val dimensionMap = new util.BitSet(java.lang.Long.SIZE << 4)
  private val logger = LogManager.getLogger

  if(!defaultsRegistered){
    this.registerProviderType(0, classOf[WorldProviderSurface], keepLoaded = true)
    this.registerDimension(0, 0)

    defaultsRegistered = true
  }

  def registerProviderType(id: Int, provider: Class[_ <: WorldProvider], keepLoaded: Boolean = false): Boolean = {
    if(providers.containsKey(id)) return false
    providers.put(id, provider)
    spawnSettings.put(id, keepLoaded)
    true
  }

  /**
   * Unregisters a Provider type, and returns an array of all dimensions that are
   * registered to this provider type.
   * If the return size is greater then 0, it is required that the caller either
   * changes those dimensions's registered type, or replace this type before the
   * world is attempted to load, else the loader will throw an exception.
   *
   * @param id The provider type ID to unreigster
   * @return An array containing all dimension IDs still registered to this provider type.
   */
  def unregisterProviderType(id: Int): Array[Int] = {
    if(!providers.containsKey(id)) return new Array[Int](0)
    providers.remove(id)
    spawnSettings.remove(id)
    val ret = new Array[Int](dimensions.size())
    var x = 0
    for(e <- dimensions.entrySet()){
      if(e.getValue == id){
        ret(x) = e.getKey
        x += 1
      }
    }
    util.Arrays.copyOf(ret, x)
  }

  def registerDimension(id: Int, providerType: Int){
    if(!providers.containsKey(providerType)){
      throw new IllegalArgumentException("Failed to register dimension for id %d, provider type %d does not exist".format(id, providerType))
    }
    if(dimensions.containsKey(id)){
      throw new IllegalArgumentException("Failed to register dimension for id %d, One is already registered".format(id))
    }
    dimensions.put(id, providerType)
    if(id >= 0) dimensionMap.set(id)
  }

  /**
   * For unregistering a dimension when the save is changed (disconnected from a server or loaded a new save
   */
  def unregisterDimension(id: Int){
    if(!dimensions.containsKey(id)){
      throw new IllegalArgumentException("Failed to unregister dimension for id %d; No provider registered".format(id))
    }
    dimensions.remove(id)
  }

  def isDimensionRegistered(dim: Int) = dimensions.containsKey(dim)

  def getProviderType(dim: Int): Int = {
    if(!dimensions.containsKey(dim)) throw new IllegalArgumentException("Could not get provider type for dimension %d, does not exist".format(dim))
    dimensions.get(dim)
  }

  def getAllDimensionIds: Array[Int] = this.vanillaWorlds.keySet().asScala.toArray

  def setWorld(id: Int, world: WorldServer){
    if(world != null){
      this.vanillaWorlds.put(id, world)
      MinecraftServer.getServer.worldTickTimes.put(id, new Array[Long](100))
      logger.info(s"Loading dimension $id (${world.getWorldInfo.getWorldName}) (${world.func_73046_m()})")
    }else{
      this.vanillaWorlds.remove(id)
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

  def initWorld(dimension: Int){
    val overworld = this.getVanillaWorld(0)
    if(overworld == null) throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!")
    if(!this.dimensions.containsKey(dimension)) throw new IllegalArgumentException("Provider type for dimension %d does not exist!".format(dimension))
    val mcserver = overworld.func_73046_m()
    val savehandler = overworld.getSaveHandler
    val worldSettings = new WorldSettings(overworld.getWorldInfo)
    val world = new WorldServer(mcserver, savehandler, overworld.getWorldInfo.getWorldName, dimension, worldSettings, mcserver.theProfiler)
    world.addWorldAccess(new WorldManager(mcserver, world))
    NailedEventFactory.fireWorldLoad(world)
    world.getWorldInfo.setGameType(mcserver.getGameType)
  }

  def getVanillaWorld(dimension: Int) = this.vanillaWorlds.get(dimension)

  def getVanillaWorlds = this.vanillaWorlds.values.toArray(new Array[WorldServer](this.vanillaWorlds.size()))

  def shouldKeepLoaded(dimension: Int) = {
    val id = this.getProviderType(dimension)
    this.spawnSettings.containsKey(id) && this.spawnSettings.get(id)
  }

  def createProviderFor(dim: Int): WorldProvider = try{
    if(this.dimensions.containsKey(dim)){
      val provider = this.providers.get(this.getProviderType(dim)).newInstance()
      //provider.setDimension(dim)
      provider
    }else throw new RuntimeException("No WorldProvider bound for dimension %d".format(dim))
  }catch{
    case e: Exception =>
      logger.warn(s"An error occured trying to create an instance of WorldProvider $dim (${providers.get(this.getProviderType(dim)).getName})")
      throw new RuntimeException(e)
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

  /**
   * Return the next free dimension ID. Note: you are not guaranteed a contiguous
   * block of free ids. Always call for each individual ID you wish to get.
   * @return the next free dimension ID
   */
  def getNextFreeDimensionId: Int = {
    var next = 0
    while(true){
      next = this.dimensionMap.nextClearBit(next)
      if(dimensions.containsKey(next)) dimensionMap.set(next) else return next
    }
    next
  }

  /*public static File getCurrentSaveRootDirectory()
  {
    if (DimensionManager.getWorld(0) != null)
    {
      return ((SaveHandler)DimensionManager.getWorld(0).getSaveHandler()).getWorldDirectory();
    }
    else if (MinecraftServer.getServer() != null)
    {
      MinecraftServer srv = MinecraftServer.getServer();
      SaveHandler saveHandler = (SaveHandler) srv.getActiveAnvilConverter().getSaveLoader(srv.getFolderName(), false);
      return saveHandler.getWorldDirectory();
    }
    else
    {
      return null;
    }
  }*/
}
