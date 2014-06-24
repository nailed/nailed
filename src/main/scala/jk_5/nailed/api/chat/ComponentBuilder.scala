package jk_5.nailed.api.chat

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class ComponentBuilder(text: String) {
  private var current = new TextComponent(text)
  private val parts = mutable.ArrayBuffer[BaseComponent]()

  def append(text: String): ComponentBuilder = {
    parts += current
    current = new TextComponent(current)
    current.setText(text)
    this
  }

  def color(color: ChatColor): ComponentBuilder = {
    current.setColor(color)
    this
  }

  def bold(bold: Boolean): ComponentBuilder = {
    current.setBold(bold)
    this
  }

  def italic(italic: Boolean): ComponentBuilder = {
    current.setItalic(italic)
    this
  }

  def underlined(underlined: Boolean): ComponentBuilder = {
    current.setUnderlined(underlined)
    this
  }

  def strikethrough(strikethrough: Boolean): ComponentBuilder = {
    current.setStrikethrough(strikethrough)
    this
  }

  def obfuscated(obfuscated: Boolean): ComponentBuilder = {
    current.setObfuscated(obfuscated)
    this
  }

  def event(event: HoverEvent): ComponentBuilder = {
    current.setHoverEvent(event)
    this
  }

  def event(event: ClickEvent): ComponentBuilder = {
    current.setClickEvent(event)
    this
  }

  def create(): Array[BaseComponent] = {
    parts += current
    parts.toArray
  }
}
