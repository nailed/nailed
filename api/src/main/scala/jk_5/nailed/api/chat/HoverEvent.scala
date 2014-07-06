package jk_5.nailed.api.chat

import jk_5.nailed.api.chat.HoverEventAction.HoverEventAction

/**
 * No description given
 *
 * @author jk-5
 */
final case class HoverEvent(action: HoverEventAction, value: BaseComponent) {
  override def toString = "HoverEvent{action=%s, value=%s}".format(action, value)
}

object HoverEventAction extends Enumeration {
  val SHOW_TEXT, SHOW_ACHIEVEMENT, SHOW_ITEM = Value
  type HoverEventAction = Value
}
