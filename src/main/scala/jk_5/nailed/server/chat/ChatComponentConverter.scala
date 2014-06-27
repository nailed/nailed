package jk_5.nailed.server.chat

import jk_5.nailed.api.chat._
import net.minecraft.event
import net.minecraft.util._
import org.apache.logging.log4j.LogManager

/**
 * No description given
 *
 * @author jk-5
 */
object ChatComponentConverter {

  private val logger = LogManager.getLogger

  implicit def toVanilla(component: BaseComponent): IChatComponent = {
    val base = singleComponentToVanilla(component)
    for(child <- component.getChildren){
      base.appendSibling(toVanilla(child))
    }
    base
  }

  def singleComponentToVanilla(component: BaseComponent): IChatComponent = {
    val base = component match {
      case t: TextComponent => new ChatComponentText(t.getText)
      case t: TranslatableComponent => new ChatComponentTranslation(t.getTranslate, t.getReplacements)
      case t =>
        logger.warn("Was not able to convert component {0} to vanilla", t.toString)
        return null
    }
    val style = new ChatStyle
    style.setColor(component.getColorRaw)
    style.setBold(component.isBoldRaw)
    style.setItalic(component.isBoldRaw)
    style.setUnderlined(component.isBoldRaw)
    style.setStrikethrough(component.isBoldRaw)
    style.setObfuscated(component.isObfuscatedRaw)
    if(component.getHoverEvent != null){
      val e = component.getHoverEvent
      val newAction = e.action match {
        case HoverEventAction.SHOW_ACHIEVEMENT => event.HoverEvent.Action.SHOW_ACHIEVEMENT
        case HoverEventAction.SHOW_ITEM => event.HoverEvent.Action.SHOW_ITEM
        case HoverEventAction.SHOW_TEXT => event.HoverEvent.Action.SHOW_TEXT
        case _ => null
      }
      if(newAction != null){
        style.setChatHoverEvent(new event.HoverEvent(newAction, singleComponentToVanilla(e.value)))
      }
    }
    if(component.getClickEvent != null){
      val e = component.getClickEvent
      val newAction = e.action match {
        case ClickEventAction.OPEN_FILE => event.ClickEvent.Action.OPEN_FILE
        case ClickEventAction.OPEN_URL => event.ClickEvent.Action.OPEN_URL
        case ClickEventAction.RUN_COMMAND => event.ClickEvent.Action.RUN_COMMAND
        case ClickEventAction.SUGGEST_COMMAND => event.ClickEvent.Action.SUGGEST_COMMAND
        case _ => null
      }
      if(newAction != null){
        style.setChatClickEvent(new event.ClickEvent(newAction, e.value))
      }
    }
    base.setChatStyle(style)
    base
  }

  @inline implicit def convertColor(color: ChatColor): EnumChatFormatting = if(color == null) null else EnumChatFormatting.values().find(c => c.getFormattingCode == color.code).orNull
}
