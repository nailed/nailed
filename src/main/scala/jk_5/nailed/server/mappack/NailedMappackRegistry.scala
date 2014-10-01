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

import jk_5.nailed.api.Platform
import jk_5.nailed.api.event.mappack.{MappackRegisteredEvent, MappackUnregisteredEvent}
import jk_5.nailed.api.mappack.{Mappack, MappackRegistry}
import jk_5.nailed.server.NailedPlatform

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
      NailedPlatform.globalEventBus.post(new MappackRegisteredEvent(mappack))
      true
    }

  override def getByName(name: String): Mappack = this.mappacks.get(name).orNull

  override def getByType[T](cl: Class[_ <: T]): java.util.Collection[T] = {
    java.util.Arrays.asList(this.mappacks.values.collect {
      case special if cl.isAssignableFrom(special.getClass) => special
      case _ =>
    }.toArray.asInstanceOf[Array[T]]: _*)
  }

  override def getAll: java.util.Collection[Mappack] = java.util.Arrays.asList(this.mappacks.values.toArray: _*)
  override def getAllIds: java.util.Collection[String] = java.util.Arrays.asList(this.mappacks.values.toArray.map(_.getId): _*)

  override def unregister(mappack: Mappack): Boolean = {
    if(!this.mappacks.exists(p => p._1 == mappack.getId && p._2 == mappack)) false
    else{
      this.mappacks.remove(mappack.getId)
      NailedPlatform.globalEventBus.post(new MappackUnregisteredEvent(mappack))
      true
    }
  }
}

trait MappackRegistryTrait extends Platform {
  override def getMappackRegistry = NailedMappackRegistry
}
