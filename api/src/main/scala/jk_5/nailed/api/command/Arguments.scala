package jk_5.nailed.api.command

import jk_5.nailed.api.Server
import jk_5.nailed.api.mappack.Mappack
import jk_5.nailed.api.player.Player

/**
 * No description given
 *
 * @author jk-5
 */
final class Arguments(private val ctx: CommandContext, private val args: Array[String]) {

  def arguments = args
  def amount = args.length

  private def argAt(index: Int, name: String, t: Boolean): String = {
    if(index >= args.length){
      if(t) throw ctx.error(s"Missing $name argument at index $index") else null
    }else{
      args(index)
    }
  }

  private def parseNumber[T](index: Int, toosmall: (T) => Boolean, toobig: (T) => Boolean, convert: (String) => T, default: T, d: T)(implicit mf: Manifest[T]): T = {
    val a = argAt(index, "number", default != d)
    try{
      val int = convert(a)
      if(toobig(int)){
        throw ctx.error(s"Number $a is bigger than the maximum ($toobig)")
      }else if(toosmall(int)){
        throw ctx.error(s"Number $a is smaller than the minimum ($toosmall)")
      }else{
        int
      }
    }catch{
      case _: NumberFormatException =>
        if(default == d){
          throw ctx.error(s"Entered value $a is not a valid " + mf.runtimeClass.getSimpleName.toLowerCase)
        }else{
          default
        }
    }
  }

  def getString(index: Int, description: String = null, default: String = null): String = {
    val a = argAt(index, "string" + (if(description != null) s" ($description)" else ""), default == null)
    if(a == null) default else a
  }
  def getSpacedString(index: Int, description: String = null, default: String = null): String = {
    if(index >= args.length){
      if(default == null){
        throw ctx.error(s"Missing string ($description) argument at index $index")
      } else default
    }else{
      val newArray = new Array[String](args.length - index)
      System.arraycopy(args, index, newArray, 0, newArray.length)
      newArray.mkString(" ")
    }
  }
  def getInt(index: Int, min: Int = Int.MinValue, max: Int = Int.MaxValue, default: Int = Int.MinValue): Int = parseNumber[Int](index, _ < min, _ > max, _.toInt, default, Int.MinValue)
  def getFloat(index: Int, min: Float = Float.MinValue, max: Float = Float.MaxValue, default: Float = Float.MinValue): Float = parseNumber[Float](index, _ < min, _ > max, _.toFloat, default, Float.MinValue)
  def getDouble(index: Int, min: Double = Double.MinValue, max: Double = Double.MaxValue, default: Double = Double.MinValue): Double = parseNumber[Double](index, _ < min, _ > max, _.toDouble, default, Double.MinValue)
  def getLong(index: Int, min: Long = Long.MinValue, max: Long = Long.MaxValue, default: Long = Long.MinValue): Long = parseNumber[Long](index, _ < min, _ > max, _.toLong, default, Long.MinValue)
  def getShort(index: Int, min: Short = Short.MinValue, max: Short = Short.MaxValue, default: Short = Short.MinValue): Short = parseNumber[Short](index, _ < min, _ > max, _.toShort, default, Short.MinValue)
  def getByte(index: Int, min: Byte = Byte.MinValue, max: Byte = Byte.MaxValue, default: Byte = Byte.MinValue): Byte = parseNumber[Byte](index, _ < min, _ > max, _.toByte, default, Byte.MinValue)
  def getPlayers(index: Int): Array[Player] = {
    val a = argAt(index, "player", t = true)
    ctx.sender match {
      case p: LocationCommandSender => Server.getInstance.getPlayerSelector.matchPlayers(a, p.getLocation)
      case _ => throw ctx.error("Only players can use selectors (this is being changed)")
    }
  }
  def getPlayer(index: Int): Player = {
    val a = argAt(index, "player", t = true)
    ctx.sender match {
      case p: LocationCommandSender =>
        val r = Server.getInstance.getPlayerSelector.matchPlayers(a, p.getLocation)
        if(r.length == 0) throw ctx.error(s"Player $a is not online") else r(0)
      case _ => throw ctx.error("Only players can use selectors (this is being changed)")
    }
  }
  def getMappack(index: Int): Mappack = {
    val name = argAt(index, "mappack", t = true)
    val mappack = Server.getInstance.getMappackRegistry.getByName(name)
    if(mappack.isEmpty) throw ctx.error("Unknown mappack " + name)
    mappack.get
  }
  def matchArgument[T](index: Int, description: String = null)(m: (String) => T): T = m(getString(index, description))
}
