package jk_5.nailed.server.tweaker.mixin

import java.io.InputStreamReader
import java.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

import scala.collection.mutable

private[mixin] object MixinConfig {

  def apply(configFile: String): MixinConfig = {
    try{
      new Gson().fromJson(new InputStreamReader(this.getClass.getResourceAsStream("/" + configFile)), classOf[MixinConfig])
    }catch{
      case e: Exception =>
        e.printStackTrace()
        throw new IllegalArgumentException("The specified configuration file '%s' was invalid or could not be read".format(configFile))
    }
  }

}

private[mixin] class MixinConfig {

  @transient
  private val mixinMapping = mutable.HashMap[String, util.List[MixinInfo]]()

  @SerializedName("package")
  private var mixinPackage: String = _

  @SerializedName("mixins")
  private var mixinClasses: Array[String] = _

  @SerializedName("setSourceFile")
  private var setSourceFile = false

  @transient
  private var initialized = false

  def init(){
    initialized = true
    if(!this.mixinPackage.endsWith(".")){
      this.mixinPackage += "."
    }
    this.mixinClasses foreach { c =>
      try{
        val mixin = new MixinInfo(this.mixinPackage + c, true)
        mixin.getTargetClasses foreach { tc =>
          this.getMixinsFor(tc).add(mixin)
        }
      }catch{
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def getMixinPackage = this.mixinPackage
  def getClasses = this.mixinClasses
  def shouldSetSourceFile = this.setSourceFile

  def hasMixinsFor(targetClass: String): Boolean = {
    if(!this.initialized) this.init()
    this.mixinMapping.contains(targetClass)
  }

  def getMixinsFor(targetClass: String): util.List[MixinInfo] = {
    if(!this.initialized) this.init()
    var mixins = this.mixinMapping.getOrElse(targetClass, null)
    if(mixins == null){
      mixins = new util.ArrayList[MixinInfo]()
      this.mixinMapping.put(targetClass, mixins)
    }
    mixins
  }
}
