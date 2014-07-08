package jk_5.nailed.api.util

import com.google.gson.JsonObject
import jk_5.nailed.api.world.World

/**
 * No description given
 *
 * @author jk-5
 */
object Location {
  def read(json: JsonObject) = new Location(
    null,
    if(json.has("x")) json.get("x").getAsDouble else 0,
    if(json.has("y")) json.get("y").getAsDouble else 64,
    if(json.has("z")) json.get("z").getAsDouble else 0,
    if(json.has("yaw")) json.get("yaw").getAsFloat else 0,
    if(json.has("pitch")) json.get("pitch").getAsFloat else 0
  )
}
case class Location (
  private var world: World,
  private var x: Double,
  private var y: Double,
  private var z: Double,
  private var yaw: Float = 0,
  private var pitch: Float = 0
) extends Cloneable {
  def this(location: Location) = this(location.world, location.x, location.y, location.z, location.yaw, location.pitch)

  def setWorld(world: World) = this.world = world
  def setX(x: Double) = this.x = x
  def setY(y: Double) = this.y = y
  def setZ(z: Double) = this.z = z
  def setYaw(yaw: Float) = this.yaw = yaw
  def setPitch(pitch: Float) = this.pitch = pitch

  def getWorld = this.world
  def getX = this.x
  def getY = this.y
  def getZ = this.z
  def getBlockX = this.locToBlock(x)
  def getBlockY = this.locToBlock(y)
  def getBlockZ = this.locToBlock(z)
  def getYaw = this.yaw
  def getPitch = this.pitch

  def getDirection: Vector = {
    val vector = new Vector

    val rotX = this.getYaw
    val rotY = this.getPitch

    vector.setY(-Math.sin(Math.toRadians(rotY)))

    val xz = Math.cos(Math.toRadians(rotY))

    vector.setX(-xz * Math.sin(Math.toRadians(rotX)))
    vector.setZ(xz * Math.cos(Math.toRadians(rotX)))

    vector
  }

  def setDirection(vec: Vector): Location = {
    val _2PI = 2 * Math.PI
    val x = vec.getX
    val z = vec.getZ

    if(x == 0 && z == 0){
      pitch = if(vec.getY > 0) -90 else 90
      return this
    }

    val theta = Math.atan2(-x, z)
    yaw = Math.toDegrees((theta + _2PI) % _2PI).toFloat

    val x2 = square(x)
    val z2 = square(z)
    val xz = Math.sqrt(x2 + z2)
    pitch = Math.toDegrees(Math.atan(-vec.getY / xz)).toFloat

    this
  }

  def add(location: Location): Location = {
    if(location == null || location.getWorld != getWorld){
      throw new IllegalArgumentException("Cannot add Locations of differing worlds")
    }

    x += location.x
    y += location.y
    z += location.z
    this
  }

  def add(vec: Vector): Location = {
    x += vec.getX
    y += vec.getY
    z += vec.getZ
    this
  }

  def add(x: Double, y: Double, z: Double): Location = {
    this.x += x
    this.y += y
    this.z += z
    this
  }

  def subtract(location: Location): Location = {
    if(location == null || location.getWorld != getWorld){
      throw new IllegalArgumentException("Cannot subtract Locations of differing worlds")
    }

    x -= location.x
    y -= location.y
    z -= location.z
    this
  }

  def subtract(vec: Vector): Location = {
    x -= vec.getX
    y -= vec.getY
    z -= vec.getZ
    this
  }

  def subtract(x: Double, y: Double, z: Double): Location = {
    this.x -= x
    this.y -= y
    this.z -= z
    this
  }

  def length = Math.sqrt(square(x) + square(y) + square(z))
  def lengthSquared = square(x) + square(y) + square(z)

  def distance(location: Location): Double = Math.sqrt(distanceSquared(location))
  def distanceSquared(location: Location): Double = {
    if(location == null){
      throw new IllegalArgumentException("Cannot measure distance to a null location")
    }else if(location.getWorld == null || getWorld == null){
      throw new IllegalArgumentException("Cannot measure distance to a null world")
    }else if(location.getWorld != getWorld){
      throw new IllegalArgumentException("Cannot measure distance between " + getWorld.getName + " and " + location.getWorld.getName)
    }
    square(x - location.x) + square(y - location.y) + square(z - location.z)
  }

  def multiply(m: Double): Location = {
    x *= m
    y *= m
    z *= m
    this
  }

  def zero(): Location = {
    x = 0
    y = 0
    z = 0
    this
  }

  override def toString = s"Location{world=$world,x=$x,y=$y,z=$z,pitch=$pitch,yaw=$yaw}"

  def toVector = new Vector(x, y, z)

  private final def locToBlock(in: Double): Int = {
    val floor = in.toInt
    if(floor == in) floor else floor - (java.lang.Double.doubleToRawLongBits(in) >>> 63).toInt
  }

  @inline private final def square(in: Double) = in * in
}
