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

package jk_5.nailed.api.util

import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
case class Vector (protected var x: Double = 0, protected var y: Double = 0, protected var z: Double = 0) {

  def add(vec: Vector): Vector = {
    x += vec.x
    y += vec.y
    z += vec.z
    this
  }

  def subtract(vec: Vector): Vector = {
    x -= vec.x
    y -= vec.y
    z -= vec.z
    this
  }

  def multiply(vec: Vector): Vector = {
    x *= vec.x
    y *= vec.y
    z *= vec.z
    this
  }

  def divide(vec: Vector): Vector = {
    x /= vec.x
    y /= vec.y
    z /= vec.z
    this
  }

  def copy = new Vector(x, y, z)

  def length = Math.sqrt(square(x) + square(y) + square(z))
  def lengthSquared = square(x) + square(y) + square(z)

  def distance(vec: Vector) = Math.sqrt(square(x - vec.x) + square(y - vec.y) + square(z - vec.z))
  def distanceSquared(vec: Vector) = square(x - vec.x) + square(y - vec.y) + square(z - vec.z)

  def angle(vec: Vector) = {
    Math.acos(dot(vec) / (length * vec.length)).toFloat
  }

  def midpoint(vec: Vector): Vector = {
    x = (x + vec.x) / 2
    y = (y + vec.y) / 2
    z = (z + vec.z) / 2
    this
  }

  def getMidpoint(vec: Vector) = this.copy.midpoint(vec)

  def multiply(m: Int): Vector = {
    x *= m
    y *= m
    z *= m
    this
  }

  def multiply(m: Double): Vector = {
    x *= m
    y *= m
    z *= m
    this
  }

  def multiply(m: Float): Vector = {
    x *= m
    y *= m
    z *= m
    this
  }

  def divide(d: Int): Vector = {
    x /= d
    y /= d
    z /= d
    this
  }

  def divide(d: Double): Vector = {
    x /= d
    y /= d
    z /= d
    this
  }

  def divide(d: Float): Vector = {
    x /= d
    y /= d
    z /= d
    this
  }

  def dot(vec: Vector): Double = x * vec.x + y * vec.y + z * vec.z

  def crossProduct(vec: Vector): Vector = {
    val newX = y * vec.z - vec.y * z
    val newY = z * vec.x - vec.z * x
    val newZ = x * vec.y - vec.x * y

    x = newX
    y = newY
    z = newZ
    this
  }

  def normalize(): Vector = this.divide(this.length)

  def zero(): Vector = {
    x = 0
    y = 0
    z = 0
    this
  }

  def isInBox(min: Vector, max: Vector): Boolean = {
    x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z
  }

  def isInSphere(origin: Vector, radius: Double): Boolean = {
    (square(origin.x - x) + square(origin.y - y) + square(origin.z - z)) <= square(radius)
  }

  def getX = this.x
  def getY = this.y
  def getZ = this.z
  def getBlockX = locToBlock(this.x)
  def getBlockY = locToBlock(this.y)
  def getBlockZ = locToBlock(this.z)
  def setX(x: Double) = {this.x = x; this}
  def setY(y: Double) = {this.y = y; this}
  def setZ(z: Double) = {this.z = z; this}

  override def toString = s"$x,$y,$z"

  def toLocation(world: World) = new Location(world, x, y, z)
  def toLocation(world: World, yaw: Float, pitch: Float) = new Location(world, x, y, z, yaw, pitch)

  @inline private final def square(in: Double) = in * in

  private final def locToBlock(in: Double): Int = {
    val floor = in.toInt
    if(floor == in) floor else floor - (java.lang.Double.doubleToRawLongBits(in) >>> 63).toInt
  }
}
