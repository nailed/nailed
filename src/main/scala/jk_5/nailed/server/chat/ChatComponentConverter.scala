/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.chat

import jk_5.nailed.api.chat._
import net.minecraft.event
import net.minecraft.util._
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object ChatComponentConverter {

  private val logger = LogManager.getLogger

  implicit def toVanilla(component: BaseComponent): IChatComponent = {
    val base = singleComponentToVanilla(component)
    if(component.getExtra != null){
      for(child <- component.getExtra){
        base.appendSibling(toVanilla(child))
      }
    }
    base
  }

  implicit def arrayToVanilla(comp: Array[BaseComponent]): IChatComponent = {
    val base = new ChatComponentText("")
    for(c <- comp){
      base.appendSibling(c)
    }
    base
  }

  def singleComponentToVanilla(component: BaseComponent): IChatComponent = {
    val base = component match {
      case t: TextComponent => new ChatComponentText(t.getText)
      case t: TranslatableComponent => new ChatComponentTranslation(t.getTranslate, t.getWith)
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
      val newAction = e.getAction match {
        case HoverEvent.Action.SHOW_ACHIEVEMENT => event.HoverEvent.Action.SHOW_ACHIEVEMENT
        case HoverEvent.Action.SHOW_ITEM => event.HoverEvent.Action.SHOW_ITEM
        case HoverEvent.Action.SHOW_TEXT => event.HoverEvent.Action.SHOW_TEXT
        case _ => null
      }
      if(newAction != null){
        style.setChatHoverEvent(new event.HoverEvent(newAction, e.getValue))
      }
    }
    if(component.getClickEvent != null){
      val e = component.getClickEvent
      val newAction = e.getAction match {
        case ClickEvent.Action.OPEN_FILE => event.ClickEvent.Action.OPEN_FILE
        case ClickEvent.Action.OPEN_URL => event.ClickEvent.Action.OPEN_URL
        case ClickEvent.Action.RUN_COMMAND => event.ClickEvent.Action.RUN_COMMAND
        case ClickEvent.Action.SUGGEST_COMMAND => event.ClickEvent.Action.SUGGEST_COMMAND
        case _ => null
      }
      if(newAction != null){
        style.setChatClickEvent(new event.ClickEvent(newAction, e.getValue))
      }
    }
    base.setChatStyle(style)
    base
  }

  @inline implicit def convertColor(color: ChatColor): EnumChatFormatting = if(color == null) null else EnumChatFormatting.values().find(c => c.toString() == "\u00a7" + color.getCode).orNull
}
