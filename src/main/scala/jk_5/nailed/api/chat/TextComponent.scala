package jk_5.nailed.api.chat

import java.util.regex.Pattern

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object TextComponent {

  private val url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$")

  def fromLegacyText(message: String): Array[TextComponent] = {
    val components = mutable.ArrayBuffer[TextComponent]()
    var builder = new StringBuilder
    var component = new TextComponent
    val matcher = url.matcher(message)

    var i = 0
    while(i < message.length){
      var c = message.charAt(i)
      if(c == ChatColor.colorChar){
        i += 1
        c = message.charAt(i)
        if(c >= 'A' && c <= 'Z') c = Character.toLowerCase(c) //Make it lowercase
        var format = ChatColor.getByChar(c)
        if(format != null){
          if(builder.length > 0){
            val old = component
            component = new TextComponent(old)
            old.setText(builder.toString())
            builder = new StringBuilder
            components += old
          }
          format match {
            case ChatColor.bold => component.setBold(bold = true)
            case ChatColor.italic => component.setItalic(italic = true)
            case ChatColor.underline => component.setUnderlined(underlined = true)
            case ChatColor.strikethrough => component.setStrikethrough(strikethrough = true)
            case ChatColor.magic => component.setObfuscated(obfuscated = true)
            case ChatColor.red =>
              format = ChatColor.white
              component = new TextComponent
              component.setColor(format)
            case _ =>
              component = new TextComponent
              component.setColor(format)
          }
        }else{
          var pos = message.indexOf(' ', i)
          if(pos == -1) pos = message.length
          if(matcher.region(i, pos).find()){ //Url matching
            if(builder.length > 0){
              val old = component
              component = new TextComponent(old)
              old.setText(builder.toString())
              builder = new StringBuilder
              components += old
            }
            val old = component
            component = new TextComponent(old)
            val urlString = message.substring(i, pos)
            component.setText(urlString)
            component.setClickEvent(new ClickEvent(ClickEventAction.OPEN_URL, if(urlString.startsWith("http")) urlString else "http://" + urlString))
            components += component
            i += pos - i - 1
            component = old
          }else{
            builder.append(c)
          }
        }
      }
      i += 1
    }

    if(builder.length > 0){
      component.setText(builder.toString())
      components += component
    }

    //The client will crash if the array is empty
    if(components.size == 0){
      components += new TextComponent("")
    }
    components.toArray
  }
}

class TextComponent(parent: TextComponent) extends BaseComponent(parent) {
  if(parent != null) this.setText(parent.getText)

  private var text: String = _

  def this(text: String){
    this(null: TextComponent)
    setText(text)
  }

  def this(children: BaseComponent*){
    this(null: TextComponent)
    setText("")
    setChildren(mutable.ArrayBuffer(children: _*))
  }

  override private[chat] def toPlainText(builder: StringBuilder){
    builder.append(this.text)
    super.toPlainText(builder)
  }

  override private[chat] def toLegacyText(builder: StringBuilder){
    builder.append(this.getColor)
    if(this.isBold) builder.append(ChatColor.bold)
    if(this.isItalic) builder.append(ChatColor.italic)
    if(this.isUnderlined) builder.append(ChatColor.underline)
    if(this.isStrikethrough) builder.append(ChatColor.strikethrough)
    if(this.isObfuscated) builder.append(ChatColor.magic)
    builder.append(this.text)
    super.toLegacyText(builder)
  }

  override def toString = "TextComponent{text=%s, %s}".format(this.text, super.toString)

  def getText = this.text
  def setText(text: String) = this.text = text
}
