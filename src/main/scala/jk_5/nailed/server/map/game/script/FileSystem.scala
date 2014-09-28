package jk_5.nailed.server.map.game.script

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}
import java.util
import java.util.regex.Pattern

import jk_5.nailed.api.mappack.filesystem.IMount
import jk_5.nailed.api.util.Checks
import org.apache.commons.io.FilenameUtils

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object FileSystem {

  private def sanitizePath(path: String, allowWildcards: Boolean = false): String = {
    var p = path.replace('\\', '/')
    val specialChars = Array[Char]('"', ':', '<', '>', '?', '|')
    val cleanName = new StringBuilder
    for(i <- 0 until p.length){
      val c = path.charAt(i)
      if(c >= ' ' && util.Arrays.binarySearch(specialChars, c) < 0 && (allowWildcards || c != '*')){
        cleanName.append(c)
      }
    }

    p = cleanName.toString()

    val parts = p.split("/")
    val outputParts = new util.Stack[String]()
    for(i <- 0 until parts.length){
      val part = parts(i)
      if(part.length() != 0 && part != "."){
        if(part == ".."){
          if(!outputParts.empty()){
            val top = outputParts.peek()
            if(!"..".equals(top)){
              outputParts.pop()
            }else{
              outputParts.push("..")
            }
          }else{
            outputParts.push("..")
          }
        }else if(part.length() >= 255){
          outputParts.push(part.substring(0, 255))
        }else{
          outputParts.push(part)
        }
      }
    }

    val result = new mutable.StringBuilder("")
    val it = outputParts.iterator()
    while(it.hasNext){
      val part = it.next()
      result.append(part)
      if(it.hasNext){
        result.append('/')
      }
    }

    result.toString()
  }

  def contains(pathA: String, pathB: String): Boolean = {
    val a = sanitizePath(pathA)
    val b = sanitizePath(pathB)

    pathB != ".." && !pathB.startsWith("../") && (pathB == pathA || pathA.length == 0 || pathB.startsWith(pathA + "/"))
  }

  def toLocal(path: String, location: String): String = {
    val p = sanitizePath(path)
    val loc = sanitizePath(location)
    assert(contains(location, path))
    val local = p.substring(loc.length)
    if(local.startsWith("/")){
      local.substring(1)
    }else local
  }
}

class FileSystem {

  private val mounts = mutable.HashMap[String, MountWrapper]()
  private val openFiles = mutable.HashSet[IMountedFile]()

  def unload(){
    openFiles.foreach{ f => try{
      f.close()
    }catch{
      case e: IOException => //Nom nom nom
    }}
  }

  def unmount(path: String){
    val p = FileSystem.sanitizePath(path)
    if(mounts.contains(p)) this.mounts.remove(p)
  }

  def list(path: String): Array[String] = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    val list = new util.ArrayList[String]()
    mount.list(p, list)
    for(m <- this.mounts.valuesIterator){
      if(getDirectory(m.location) == p){
        list.add(getName(m.location))
      }
    }
    val array = new Array[String](list.size())
    list.toArray(array)
    array
  }

  private def findIn(dir: String, matches: mutable.ArrayBuffer[String], pattern: Pattern){
    val list = this.list(dir)
    for(i <- 0 until list.length){
      val e = list(i)
      val epath = dir + "/" + e
      if(pattern.matcher(epath).matches()){
        matches += epath
      }
      if(isDir(epath)){
        findIn(epath, matches, pattern)
      }
    }
  }

  def find(wildPath: String): Array[String] = {
    val p = FileSystem.sanitizePath(wildPath)
    val pattern = Pattern.compile("^\\Q" + p.replaceAll("\\*", "\\\\E[^\\\\/]*\\\\Q") + "\\E$")
    val matches = mutable.ArrayBuffer[String]()
    findIn("", matches, pattern)
    matches.toArray
  }

  def getDirectory(path: String): String = {
    val p = FileSystem.sanitizePath(path)
    if(p.length == 0) return ".."
    val lastSlash = p.indexOf('/')
    if(lastSlash >= 0){
      p.substring(0, lastSlash)
    }else ""
  }

  def getName(path: String): String = {
    val p = FileSystem.sanitizePath(path)
    if(p.length == 0) return "root"
    val lastSlash = p.indexOf('/')
    if(lastSlash >= 0){
      FilenameUtils.removeExtension(p.substring(lastSlash + 1))
    }else FilenameUtils.removeExtension(p)
  }

  def getSize(path: String): Long = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    mount.getSize(p)
  }

  def isDir(path: String): Boolean = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    mount.isDirectory(p)
  }

  def exists(path: String): Boolean = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    mount.exists(p)
  }

  def openAsInputStream(path: String): InputStream = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    val stream = mount.openForRead(p)
    if(stream == null) return null
    val ret = new InputStream with IMountedFile {
      override def read() = stream.read()
      override def read(b: Array[Byte]) = stream.read(b)
      override def read(b: Array[Byte], off: Int, len: Int) = stream.read(b, off, len)
      override def skip(n: Long) = stream.skip(n)
      override def available() = stream.available()
      override def mark(readlimit: Int) = stream.mark(readlimit)
      override def reset() = stream.reset()
      override def markSupported() = stream.markSupported()
      override def close(){
        openFiles -= this
        stream.close()
      }
    }
    openFiles += ret
    ret
  }

  def openForTextRead(path: String): IMountedFileText = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    val stream = mount.openForRead(p)
    if(stream == null) return null
    val reader = new BufferedReader(new InputStreamReader(stream))
    val file = new IMountedFileText {
      override def readLine() = reader.readLine()
      override def close(){
        openFiles -= this
        reader.close()
      }
    }
    openFiles += file
    file
  }

  def openForBinaryRead(path: String): IMountedFileBinary = {
    val p = FileSystem.sanitizePath(path)
    val mount = getMount(p)
    val stream = mount.openForRead(p)
    if(stream == null) return null
    val file = new IMountedFileBinary {
      override def read() = stream.read()
      override def close(){
        openFiles -= this
        stream.close()
      }
    }
    openFiles += file
    file
  }

  def mount(location: String, mount: IMount){
    Checks.notNull(location, "Location is null")
    Checks.notNull(mount, "Mount is null")
    val loc = FileSystem.sanitizePath(location)
    if(loc.contains("..")){
      throw new FileSystemException("Cannot mount below root")
    }
    this.mounts.put(loc, new MountWrapper(loc, mount))
  }

  private def getMount(path: String): MountWrapper = {
    var m: MountWrapper = null
    var mL: Int = 999
    for(mount <- this.mounts.valuesIterator){
      if(FileSystem.contains(mount.location, path)){
        val len = FileSystem.toLocal(path, mount.location).length
        if(m == null || len < mL){
          m = mount
          mL = len
        }
      }
    }
    if(m == null) throw new FileSystemException("Invalid path")
    m
  }
}
