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

package jk_5.nailed.server.tweaker

import java.io.{InputStream, InputStreamReader}

import com.google.gson.{JsonObject, JsonParser}

/**
 * No description given
 *
 * @author jk-5
 */
object NailedVersion {

  val jsonParser = new JsonParser

  var major: Int = _
  var minor: Int = _
  var revision: Int = _
  var isSnapshot: Boolean = _
  var full: String = _
  var mcversion: String = _

  def readConfig(){
    var is: InputStream = null
    var data: JsonObject = null
    try{
      is = this.getClass.getClassLoader.getResourceAsStream("nailedversion.json")
      data = jsonParser.parse(new InputStreamReader(is)).getAsJsonObject
      is.close()
    }finally{
      if(is != null) is.close()
    }
    if(data == null) throw new RuntimeException("Could not read nailedversion.json")

    val version = data.getAsJsonObject("version")
    major = version.get("major").getAsInt
    minor = version.get("minor").getAsInt
    revision = version.get("revision").getAsInt
    isSnapshot = version.get("isSnapshot").getAsBoolean
    full = version.get("full").getAsString
    mcversion = data.get("mcversion").getAsString
  }
}
