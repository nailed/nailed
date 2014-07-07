package jk_5.nailed.server.mappack

import jk_5.nailed.api.Server
import jk_5.nailed.api.event.{MappackRegisteredEvent, MappackUnregisteredEvent}
import jk_5.nailed.api.mappack.{Mappack, MappackRegistry}
import jk_5.nailed.server.NailedServer

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedMappackRegistry extends MappackRegistry {

  private val mappacks = mutable.HashMap[String, Mappack]()

  override def register(mappack: Mappack): Boolean =
    if(this.mappacks.exists(p => p._1 == mappack.getId || p._2 == mappack)) false
    else{
      this.mappacks.put(mappack.getId, mappack)
      NailedServer.getPluginManager.callEvent(new MappackRegisteredEvent(mappack))
      true
    }

  override def getByName(name: String): Option[Mappack] = this.mappacks.get(name)

  override def getByType[T <: Mappack](cl: Class[T])(implicit mf : Manifest[T]): Array[T] = {
    this.mappacks.collect({
      case special if mf.runtimeClass.isAssignableFrom(special.getClass) => special
    }).asInstanceOf[Traversable[T]].toArray
  }

  override def unregister(mappack: Mappack): Boolean = {
    if(!this.mappacks.exists(p => p._1 == mappack.getId && p._2 == mappack)) false
    else{
      this.mappacks.remove(mappack.getId)
      NailedServer.getPluginManager.callEvent(new MappackUnregisteredEvent(mappack))
      true
    }
  }
}

trait MappackRegistryTrait extends Server {
  override def getMappackRegistry = NailedMappackRegistry
}
