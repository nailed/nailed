package jk_5.nailed.api.chat

import jk_5.nailed.api.chat.ClickEventAction.ClickEventAction

/**
 * No description given
 *
 * @author jk-5
 */
final case class ClickEvent(action: ClickEventAction, value: String) {
  override def toString = "ClickEvent{action=%s, value=%s}".format(action, value)
}

object ClickEventAction extends Enumeration {
  val OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND = Value
  type ClickEventAction = Value
}
