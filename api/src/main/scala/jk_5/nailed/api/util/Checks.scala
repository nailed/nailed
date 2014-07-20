package jk_5.nailed.api.util

/**
 * No description given
 *
 * @author jk-5
 */
object Checks {

  def notNull[T](obj: T, name: String): T = {
    if(obj == null) throw new IllegalArgumentException(name)
    obj
  }

  def check(expr: Boolean, msg: String){
    if(!expr) throw new IllegalArgumentException(msg)
  }
}
