package jk_5.nailed.server.map.game.script

import java.io.{IOException, InputStream}
import java.util

import jk_5.nailed.api.map.filesystem.IMount
import jk_5.nailed.api.util.Checks

/**
 * No description given
 *
 * @author jk-5
 */
private[script] class MountWrapper(val location: String, val mount: IMount) {
  Checks.notNull(location, "Location is null")
  Checks.notNull(mount, "Mount is null")

  def exists(path: String): Boolean = {
    val p = toLocal(path)
    try{
      this.mount.exists(p)
    }catch{
      case e: IOException => throw new FileSystemException(e.getMessage)
    }
  }

  def isDirectory(path: String): Boolean = {
    val p = toLocal(path)
    try{
      this.mount.exists(p) && this.mount.isDirectory(p)
    }catch{
      case e: IOException => throw new FileSystemException(e.getMessage)
    }
  }

  def list(path: String, contents: util.List[String]){
    val p = toLocal(path)
    try{
      if(this.mount.exists(p) && this.mount.isDirectory(p)){
        this.mount.list(p, contents)
      }else{
        throw new FileSystemException("Not a directory")
      }
    }catch{
      case e: IOException => throw new FileSystemException(e.getMessage)
    }
  }

  def getSize(path: String): Long = {
    val p = toLocal(path)
    try{
      if(this.mount.exists(p)){
        if(this.mount.isDirectory(p)){
          return 0L
        }
        return this.mount.getSize(p)
      }
      throw new FileSystemException("No such file")
    }catch{
      case e: IOException => throw new FileSystemException(e.getMessage)
    }
  }

  def openForRead(path: String): InputStream = {
    val p = toLocal(path)
    try{
      if(this.mount.exists(p) && !this.mount.isDirectory(p)){
        return this.mount.openForRead(p)
      }
      throw new FileSystemException("No such file")
    }catch{
      case e: IOException => throw new FileSystemException(e.getMessage)
    }
  }

  private def toLocal(path: String) = FileSystem.toLocal(path, this.location)
}
