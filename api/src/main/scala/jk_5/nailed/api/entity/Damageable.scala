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

package jk_5.nailed.api.entity

/**
 * No description given
 *
 * @author jk-5
 */
trait Damageable {

  /**
   * Deals the given amount of damage to this entity.
   *
   * @param amount Amount of damage to deal
   */
  def damage(amount: Float)

  /**
   * Gets the entity's health from 0 to `getMaxHealth`, where 0 is dead.
   *
   * @return Health represented from 0 to max
   */
  def getHealth: Float

  /**
   * Sets the entity's health from 0 to `getMaxHealth`, where 0 is
   * dead.
   *
   * @param health New health represented from 0 to max
   * @throws IllegalArgumentException Thrown if the health is < 0 or > `getMaxHealth`
   */
  def setHealth(health: Float)

  /**
   * Gets the maximum health this entity has.
   *
   * @return Maximum health
   */
  def getMaxHealth: Float

  /**
   * Sets the maximum health this entity can have.
   * <p>
   * If the health of the entity is above the value provided it will be set
   * to that value.
   * <p>
   * Note: An entity with a health bar (`Player`) will have their bar scaled accordingly.
   *
   * @param health amount of health to set the maximum to
   */
  def setMaxHealth(health: Float)

  /**
   * Resets the max health to the original amount.
   */
  def resetMaxHealth()

  /**
   * Heals this entity to their maximum amount of health
   */
  def heal()

  /**
   * Heals this entity with the amount of half hearts
   */
  def heal(amount: Int)
}
