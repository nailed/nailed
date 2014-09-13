package jk_5.nailed.api.map.stat

import java.util

/**
 * No description given
 *
 * @author jk-5
 */
class StatEvent(val state: Boolean, val name: String, val attributes: java.util.Map[String, String]){
  def this(state: Boolean, name: String) = this(state, name, new util.HashMap[String, String]())
}
