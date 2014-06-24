package jk_5.nailed.api.plugin

import java.net.{URL, URLClassLoader}
import java.util.concurrent.CopyOnWriteArraySet

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object PluginClassLoader {
  private final val allLoaders = new CopyOnWriteArraySet[PluginClassLoader]()
  private var registered = false

  def register(): Unit = new PluginClassLoader(new Array[URL](0))
}

class PluginClassLoader(urls: Array[URL]) extends URLClassLoader(urls) {
  PluginClassLoader.allLoaders.add(this)

  if(!PluginClassLoader.registered){
    ClassLoader.registerAsParallelCapable()
    PluginClassLoader.registered = true
  }

  override def loadClass(name: String, resolve: Boolean): Class[_] = loadClass0(name, resolve, checkOther = true)

  private def loadClass0(name: String, resolve: Boolean, checkOther: Boolean): Class[_] = {
    try{
      return super.loadClass(name, resolve)
    }catch{
      case e: ClassNotFoundException =>
    }
    if(checkOther){
      for(loader <- PluginClassLoader.allLoaders){
        if(loader != this) try{
          return loader.loadClass0(name, resolve, checkOther = false)
        }catch{
          case e: ClassNotFoundException =>
        }
      }
    }
    throw new ClassNotFoundException(name)
  }
}
