package jk_5.nailed.worldedit

import java.util

import com.sk89q.jnbt._
import net.minecraft.nbt._

import scala.collection.convert.wrapAsScala._

/**
 * Converts between JNBT and Minecraft NBT classes.
 *
 * @author jk-5
 */
object NBTConverter {

  def toNative(tag: Tag): NBTBase = tag match {
    case t: IntArrayTag => toNative(t)
    case t: ListTag => toNative(t)
    case t: EndTag => toNative(t)
    case t: LongTag => toNative(t)
    case t: StringTag => toNative(t)
    case t: IntTag => toNative(t)
    case t: ByteTag => toNative(t)
    case t: ByteArrayTag => toNative(t)
    case t: CompoundTag => toNative(t)
    case t: FloatTag => toNative(t)
    case t: ShortTag => toNative(t)
    case t: DoubleTag => toNative(t)
    case t => throw new IllegalArgumentException("Can't convert tag of type " + t.getClass.getCanonicalName)
  }

  def toNative(tag: IntArrayTag): NBTTagIntArray = {
    val value = tag.getValue
    new NBTTagIntArray(util.Arrays.copyOf(value, value.length))
  }

  def toNative(tag: ListTag): NBTTagList = {
    val list = new NBTTagList
    for(child <- tag.getValue){
      if(!child.isInstanceOf[EndTag]) {
        list.appendTag(toNative(child))
      }
    }
    list
  }

  def toNative(tag: EndTag) = new NBTTagEnd
  def toNative(tag: LongTag) = new NBTTagLong(tag.getValue)
  def toNative(tag: StringTag) = new NBTTagString(tag.getValue)
  def toNative(tag: IntTag) = new NBTTagInt(tag.getValue)
  def toNative(tag: ByteTag) = new NBTTagByte(tag.getValue)
  def toNative(tag: FloatTag) = new NBTTagFloat(tag.getValue)
  def toNative(tag: ShortTag) = new NBTTagShort(tag.getValue)
  def toNative(tag: DoubleTag) = new NBTTagDouble(tag.getValue)

  def toNative(tag: ByteArrayTag): NBTTagByteArray = {
    val value = tag.getValue
    new NBTTagByteArray(util.Arrays.copyOf(value, value.length))
  }

  def toNative(tag: CompoundTag): NBTTagCompound = {
    val compound = new NBTTagCompound
    for(child <- tag.getValue.entrySet){
      compound.setTag(child.getKey, toNative(child.getValue))
    }
    compound
  }

  def fromNative(other: NBTTagCompound): CompoundTag = {
    val tags = other.getKeySet.asInstanceOf[util.Set[String]]
    val map = new util.HashMap[String, Tag]()
    for(tag <- tags) map.put(tag, fromNative(other.getCompoundTag(tag)))
    new CompoundTag(null, map)
  }
}
