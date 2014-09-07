package jk_5.nailed.api.map.filesystem

import java.io.{IOException, InputStream}
import java.util

/**
 * No description given
 *
 * @author jk-5
 */
trait IMount {

  @throws[IOException] def exists(path: String): Boolean
  @throws[IOException] def isDirectory(path: String): Boolean
  @throws[IOException] def list(path: String, contents: util.List[String])
  @throws[IOException] def getSize(path: String): Long
  @throws[IOException] def openForRead(path: String): InputStream
}
