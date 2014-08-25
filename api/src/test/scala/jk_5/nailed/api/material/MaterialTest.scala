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

import org.hamcrest.CoreMatchers._
import org.junit.{Assert, Test}

/**
 * No description given
 *
 * @author jk-5
 */
class MaterialTest {

  @Test
  def testGetByName(){
    for(m <- Material.values()){
      Assert.assertThat(Material.getMaterial(m.toString), is(m))
    }
  }

  @Test
  def testGetById(){
    for(m <- Material.values()){
      if(m.getClass.getField(m.name()).getAnnotation(classOf[Deprecated]) == null){
        Assert.assertThat(Material.getMaterial(m.getLegacyId), is(m))
      }
    }
  }

  @Test
  def testGetByOutOfRangeId(){
    Assert.assertThat(Material.getMaterial(Integer.MAX_VALUE), is(nullValue()))
    Assert.assertThat(Material.getMaterial(Integer.MIN_VALUE), is(nullValue()))
  }

  @Test
  def testGetByNameNull(){
    Assert.assertThat(Material.getMaterial(null), is(nullValue()))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testMatchMaterialByNull(){
    Material.matchMaterial(null)
  }

  @Test
  def testMatchMaterialByName(){
    for(m <- Material.values()){
      Assert.assertThat(Material.matchMaterial(m.toString), is(m))
    }
  }

  @Test
  def testMatchMaterialByLowerCaseAndSpaces(){
    for(m <- Material.values()){
      val name = m.toString.replaceAll("_", " ").toLowerCase
      Assert.assertThat(Material.matchMaterial(name), is(m))
    }
  }
}
