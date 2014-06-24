package jk_5.nailed.tweaker

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
