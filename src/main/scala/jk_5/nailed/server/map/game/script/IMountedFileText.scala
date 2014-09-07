package jk_5.nailed.server.map.game.script

import java.io.IOException

/**
  * No description given
  *
  * @author jk-5
  */
trait IMountedFileText extends IMountedFile {

  @throws[IOException] def readLine(): String
}
