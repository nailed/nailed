package jk_5.nailed.api.chat

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object BaseComponent {

  /**
   * Converts the components to a string that uses the old formatting codes
   * ({@link ChatColor#colorChar}
   *
   * @param components the components to convert
   * @return the string in the old format
   */
  def toLegacyText(components: BaseComponent*): String = {
    val builder = new StringBuilder
    components.foreach(c => builder.append(c.toLegacyText))
    builder.toString()
  }

  /**
   * Converts the components into a string without any formatting
   *
   * @param components the components to convert
   * @return the string as plain text
   */
  def toPlainText(components: BaseComponent*): String = {
    val builder = new StringBuilder
    components.foreach(c => builder.append(c.toPlainText))
    builder.toString()
  }
}

abstract class BaseComponent {

  private[chat] var parent: BaseComponent = null

  def this(copy: BaseComponent){
    this()
    if(copy != null){
      setColor(copy.getColorRaw)
      setBold(copy.isBoldRaw)
      setItalic(copy.isItalicRaw)
      setUnderlined(copy.isUnderlinedRaw)
      setStrikethrough(copy.isStrikethroughRaw)
      setObfuscated(copy.isObfuscatedRaw)
      setClickEvent(copy.getClickEvent)
      setHoverEvent(copy.getHoverEvent)
    }
  }


  /**
   * The color of this component and any child components (unless overridden)
   */
  private var color: ChatColor = _

  /**
   * Whether this component and any child components (unless overridden) is
   * bold
   */
  private var bold: java.lang.Boolean = null

  /**
   * Whether this component and any child components (unless overridden) is
   * italic
   */
  private var italic: java.lang.Boolean = null
  /**
   * Whether this component and any child components (unless overridden) is
   * underlined
   */
  private var underlined: java.lang.Boolean = null
  /**
   * Whether this component and any child components (unless overridden) is
   * strikethrough
   */
  private var strikethrough: java.lang.Boolean = null
  /**
   * Whether this component and any child components (unless overridden) is
   * obfuscated
   */
  private var obfuscated: java.lang.Boolean = null

  /**
   * Appended components that inherit this component's formatting and events
   */
  private var children = mutable.ArrayBuffer[BaseComponent]()

  /**
   * The action to preform when this component (and child components) are
   * clicked
   */
  private var clickEvent: ClickEvent = null

  /**
   * The action to preform when this component (and child components) are
   * hovered over
   */
  private var hoverEvent: HoverEvent = null

  def setColor(color: ChatColor) = this.color = color
  def setBold(bold: Boolean) = this.bold = bold
  def setItalic(italic: Boolean) = this.italic = italic
  def setUnderlined(underlined: Boolean) = this.underlined = underlined
  def setStrikethrough(strikethrough: Boolean) = this.strikethrough = strikethrough
  def setObfuscated(obfuscated: Boolean) = this.obfuscated = obfuscated
  def setClickEvent(clickEvent: ClickEvent) = this.clickEvent = clickEvent
  def setHoverEvent(hoverEvent: HoverEvent) = this.hoverEvent = hoverEvent
  def setParent(parent: BaseComponent) = this.parent = parent

  /**
   * Returns the color of this component. This uses the parent's color if this
   * component doesn't have one. {@link ChatColor#white}
   * is returned if no color is found.
   *
   * @return the color of this component
   */
  def getColor: ChatColor = {
    if(this.color == null){
      if(this.parent == null){
        return ChatColor.white
      }
      return parent.getColor
    }
    this.color
  }

  /**
   * Returns the color of this component without checking the parents color.
   * May return null
   *
   * @return the color of this component
   */
  def getColorRaw = this.color

  /**
   * Returns whether this component is bold. This uses the parent's setting if
   * this component hasn't been set. false is returned if none of the parent
   * chain has been set.
   *
   * @return whether the component is bold
   */
  def isBold: Boolean = {
    if(this.bold == null){
      parent != null && parent.isBold
    }else this.bold
  }

  /**
   * Returns whether this component is bold without checking the parents
   * setting. May return null
   *
   * @return whether the component is bold
   */
  def isBoldRaw = this.bold

  /**
   * Returns whether this component is italic. This uses the parent's setting
   * if this component hasn't been set. false is returned if none of the
   * parent chain has been set.
   *
   * @return whether the component is italic
   */
  def isItalic: Boolean = {
    if(this.italic == null){
      parent != null && parent.isItalic
    }else this.italic
  }

  /**
   * Returns whether this component is italic without checking the parents
   * setting. May return null
   *
   * @return whether the component is italic
   */
  def isItalicRaw = this.italic

  /**
   * Returns whether this component is underlined. This uses the parent's
   * setting if this component hasn't been set. false is returned if none of
   * the parent chain has been set.
   *
   * @return whether the component is underlined
   */
  def isUnderlined: Boolean = {
    if(this.underlined == null){
      parent != null && parent.isUnderlined
    }else this.underlined
  }

  /**
   * Returns whether this component is underlined without checking the parents
   * setting. May return null
   *
   * @return whether the component is underlined
   */
  def isUnderlinedRaw = this.underlined

  /**
   * Returns whether this component is strikethrough. This uses the parent's
   * setting if this component hasn't been set. false is returned if none of
   * the parent chain has been set.
   *
   * @return whether the component is strikethrough
   */
  def isStrikethrough: Boolean = {
    if(this.strikethrough == null){
      parent != null && parent.isStrikethrough
    }else this.strikethrough
  }

  /**
   * Returns whether this component is strikethrough without checking the
   * parents setting. May return null
   *
   * @return whether the component is strikethrough
   */
  def isStrikethroughRaw = this.strikethrough

  /**
   * Returns whether this component is obfuscated. This uses the parent's
   * setting if this component hasn't been set. false is returned if none of
   * the parent chain has been set.
   *
   * @return whether the component is obfuscated
   */
  def isObfuscated: Boolean = {
    if(this.obfuscated == null){
      parent != null && parent.isObfuscated
    }else this.obfuscated
  }

  /**
   * Returns whether this component is obfuscated without checking the parents
   * setting. May return null
   *
   * @return whether the component is obfuscated
   */
  def isObfuscatedRaw = this.obfuscated

  def setChildren(components: mutable.ArrayBuffer[BaseComponent]){
    this.children.foreach(_.parent = this)
    this.children = components
  }

  /**
   * Appends a text element to the component. The text will inherit this
   * component's formatting
   *
   * @param text the text to append
   */
  def addExtra(text: String): Unit = this.addExtra(new TextComponent(text))

  /**
   * Appends a component to the component. The text will inherit this
   * component's formatting
   *
   * @param component the component to append
   */
  def addExtra(component: BaseComponent){
    if(this.children == null){
      this.children = mutable.ArrayBuffer[BaseComponent]()
    }
    component.parent = this
    this.children += component
  }

  /**
   * Returns whether the component has any formatting or events applied to it
   *
   * @return
   */
  def hasFormatting: Boolean = this.color != null || this.bold != null || this.italic != null || this.underlined != null || this.strikethrough != null || this.obfuscated != null || this.hoverEvent != null || this.clickEvent != null

  /**
   * Converts the component into a string without any formatting
   *
   * @return the string as plain text
   */
  def toPlainText: String = {
    val builder = new StringBuilder
    this.toPlainText(builder)
    builder.toString()
  }

  private[chat] def toPlainText(builder: StringBuilder){
    if(this.children != null){
      this.children.foreach(_.toPlainText(builder))
    }
  }

  /**
   * Converts the component to a string that uses the old formatting codes
   * ({@link ChatColor#colorChar}
   *
   * @return the string in the old format
   */
  def toLegacyText: String = {
    val builder = new StringBuilder
    this.toLegacyText(builder)
    builder.toString()
  }

  private[chat] def toLegacyText(builder: StringBuilder){
    if(this.children != null){
      this.children.foreach(_.toLegacyText(builder))
    }
  }

  def getClickEvent = this.clickEvent
  def getHoverEvent = this.hoverEvent
  def getChildren = this.children

  override def toString = "BaseComponent{color=%s, bold=%b, italic=%b, underlined=%b, strikethrough=%b, obfuscated=%b, clickEvent=%s, hoverEvent=%s, extra=%s}".format(getColor.getName, isBold, isItalic, isUnderlined, isStrikethrough, isObfuscated, getClickEvent, getHoverEvent, getChildren)
}
