package jk_5.nailed.server.chat

import jk_5.nailed.api.chat._
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
    base.setChatStyle(style)
    base
  }

  @inline implicit def convertColor(color: ChatColor): EnumChatFormatting = if(color == null) null else EnumChatFormatting.values().find(c => c.getFormattingCode == color.code).orNull
}
