package jk_5.nailed.server.map.game.script

import java.io.IOException

/**
  * No description given
  *
  * @author jk-5
  */
trait IMountedFileBinary extends IMountedFile {

  @throws[IOException] def read(): Int
}
