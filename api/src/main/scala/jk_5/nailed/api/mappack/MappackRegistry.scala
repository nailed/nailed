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

package jk_5.nailed.api.mappack

/**
 * This class is responsible for tracking, loading or registering mappacks
 *
 * @author jk-5
 */
trait MappackRegistry {

  /**
   * Registers the mappack to the mappack system
   * It checks if a mappack with that id, or that exact mappack already exists.
   * If one (or both) of those exist, it returns false.
   * When it is successfully registered, it returns true.
   *
   * @param mappack the mappack to register to the system
   * @return true if the mappack was registered successfully, false otherwise, or when dupplicates were found
   */
  def register(mappack: Mappack): Boolean

  /**
   * Obtains the mappack with the given name
   *
   * @param name the name to return the mappack for
   * @return Some(mappack) when a mappack with the given name was found. None otherwise
   */
  def getByName(name: String): Option[Mappack]

  /**
   * Returns all the mappacks that implement or extend the given class
   *
   * @param cl the class to check for
   * @tparam T the type of the mappack class, which is the same type as will be returned
   * @return an array of all the mappacks with the given type
   */
  def getByType[T <: Mappack](cl: Class[T])(implicit mf: Manifest[T]): Array[T]

  def getAll: Array[Mappack]
  def getAllIds: Array[String]

  def unregister(mappack: Mappack): Boolean
}
