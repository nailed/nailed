package jk_5.nailed.api.map.filesystem

import java.io.{File, FileInputStream, IOException, InputStream}
import java.util

/**
 * No description given
 *
 * @author jk-5
 */
class DirectoryMount(val root: File) extends IMount {

  private def getRealPath(path: String) = new File(root, path)
  private def created: Boolean = root.exists()

  override def exists(path: String): Boolean = {
    if(!created) return path.length == 0
    getRealPath(path).exists()
  }

  override def isDirectory(path: String): Boolean = {
    if(!created) return path.length == 0
    val file = getRealPath(path)
    file.exists() && file.isDirectory
  }

  override def list(path: String, contents: util.List[String]){
    if(!created){
      if(path.length != 0) throw new IOException("Not a directory")
    }else{
      val file = getRealPath(path)
      if(file.exists() && file.isDirectory){
        val paths = file.list()
        for(sub <- paths){
          if(new File(file, sub).exists()){
            contents.add(sub)
          }
        }
      }else{
        throw new IOException("Not a directory")
      }
    }
  }

  override def getSize(path: String): Long = {
    if(!created){
      if(path.length == 0) return 0
    }else{
      val file = getRealPath(path)
      if(file.exists()){
        if(file.isDirectory) return 0
        return file.length()
      }
    }
    throw new IOException("No such file")
  }

  override def openForRead(path: String): InputStream = {
    if(created){
      val file = getRealPath(path)
      if(file.exists() && !file.isDirectory){
        return new FileInputStream(file)
      }
    }
    throw new IOException("No such file")
  }
}
