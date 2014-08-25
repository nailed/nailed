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

package jk_5.nailed.api.material

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class ItemStack(private var material: Material, private var amount: Int = 1, private var damage: Short = 0) {
  def this(itemStack: ItemStack) = this(itemStack.material, itemStack.amount, itemStack.damage)

  private var displayName: Option[String] = None
  private val lore = mutable.ArrayBuffer[String]()
  private val tags = mutable.HashMap[String, String]()

  def getMaterial = this.material
  def getAmount = this.amount
  def getDamage = this.damage

  def setMaterial(material: Material) = this.material = material
  def setAmount(amount: Int) = this.amount = amount
  def setDamage(damage: Short) = this.damage = damage

  def setDisplayName(display: String) = this.displayName = Option(display)
  def getDisplayName: Option[String] = this.displayName

  def addLore(lore: String) = this.lore += lore
  def getLore = this.lore.toArray

  def setTag(key: String, value: String) = this.tags.put(key, value)
  def getTag(key: String): Option[String] = this.tags.get(key)
  def getTags: Array[(String, String)] = this.tags.toArray

  def getMaxStackSize = if(this.material == null) -1 else this.material.getMaxStackSize

  override def toString = new StringBuilder("ItemStack{material=").append(this.getMaterial).append(",amount=").append(this.getAmount).append(",damage=").append(this.getDamage).append('}').toString

  override def equals(other: Any): Boolean = other match {
    case that: ItemStack =>
      (that canEqual this) &&
        material == that.material &&
        amount == that.amount &&
        damage == that.damage
    case _ => false
  }

  def isSimilar(other: Any): Boolean = other match {
    case that: ItemStack =>
      (that canEqual this) &&
        material == that.material &&
        damage == that.damage
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[ItemStack]

  override def hashCode(): Int = {
    val state = Seq(material, amount, damage)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
