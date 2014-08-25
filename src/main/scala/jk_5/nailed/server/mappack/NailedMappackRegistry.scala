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

  override def getByType[T <: Mappack](cl: Class[T])(implicit mf: Manifest[T]): Array[T] = {
    this.mappacks.collect({
      case special if mf.runtimeClass.isAssignableFrom(special.getClass) => special
      case _ =>
    }).asInstanceOf[Traversable[T]].toArray
  }

  override def getAll: Array[Mappack] = this.mappacks.values.toArray
  override def getAllIds: Array[String] = this.getAll.map(_.getId)

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
