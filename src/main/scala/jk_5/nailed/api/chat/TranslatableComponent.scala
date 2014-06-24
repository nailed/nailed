package jk_5.nailed.api.chat

import java.util.ResourceBundle
import java.util.regex.Pattern

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object TranslatableComponent {
  private final val locales = ResourceBundle.getBundle("assets/minecraft/lang/en_US")
  private final val format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)")
}

class TranslatableComponent(private var translate: String, rep: Any*) extends BaseComponent {

  private var replacements: mutable.ArrayBuffer[BaseComponent] =
    if(rep.size == 0){
      mutable.ArrayBuffer[BaseComponent]()
    }else{
      val ret = mutable.ArrayBuffer[BaseComponent]()
      rep.foreach{
        case s: String =>
          val c = new TextComponent(s)
          c.setParent(this)
          ret += c
        case c: BaseComponent =>
          c.setParent(this)
          ret += c
        case _ =>
      }
      ret
    }

  def setReplacements(replacements: mutable.ArrayBuffer[BaseComponent]){
    replacements.foreach(c => c.parent = this)
    this.replacements = replacements
  }

  def addReplacement(text: String) = this.addReplacement(new TextComponent(text))
  def addReplacement(replacement: BaseComponent){
    if(this.replacements == null) this.replacements = mutable.ArrayBuffer[BaseComponent]()
    replacement.parent = this
    replacements += replacement
  }

  override protected def toPlainText(builder: StringBuilder){
    val trans = TranslatableComponent.locales.getString(this.translate)
    if(trans == null){
      builder.append(this.translate)
    }else{
      val matcher = TranslatableComponent.format.matcher(trans)
      var position = 0
      var i = 0
      var break = false
      while(matcher.find(position) && !break){
        val pos = matcher.start()
        if(pos != position){
          builder.append(trans.substring(position, pos))
        }
        position = matcher.end()
        val formatCode = matcher.group(2)
        formatCode.charAt(0) match {
          case 's' | 'd' =>
            val withIndex = matcher.group(1)
            this.replacements(if(withIndex != null) Integer.parseInt(withIndex) - 1 else {i += 1; i - 1}).toPlainText(builder)
            break = true
          case '%' =>
            builder.append('%')
            break = true
        }
      }
      if(trans.length != position){
        builder.append(trans.substring(position, trans.length))
      }
    }
    super.toPlainText(builder)
  }

  override protected def toLegacyText(builder: StringBuilder){
    val trans = TranslatableComponent.locales.getString(this.translate)
    if(trans == null){
      addFormat(builder)
      builder.append(this.translate)
    }else{
      val matcher = TranslatableComponent.format.matcher(trans)
      var position = 0
      var i = 0
      var break = false
      while(matcher.find(position) && !break){
        val pos = matcher.start()
        if(pos != position){
          addFormat(builder)
          builder.append(trans.substring(position, pos))
        }
        position = matcher.end()
        val formatCode = matcher.group(2)
        formatCode.charAt(0) match {
          case 's' | 'd' =>
            val withIndex = matcher.group(1)
            this.replacements(if(withIndex != null) Integer.parseInt(withIndex) - 1 else {i += 1; i - 1}).toLegacyText(builder)
            break = true
          case '%' =>
            addFormat(builder)
            builder.append('%')
            break = true
        }
      }
      if(trans.length != position){
        addFormat(builder)
        builder.append(trans.substring(position, trans.length))
      }
    }
    super.toLegacyText(builder)
  }

  def addFormat(builder: StringBuilder){
    builder.append(getColor)
    if(isBold) builder.append(ChatColor.bold)
    if(isItalic) builder.append(ChatColor.italic)
    if(isUnderlined) builder.append(ChatColor.underline)
    if(isStrikethrough) builder.append(ChatColor.strikethrough)
    if(isObfuscated) builder.append(ChatColor.magic)
  }

  override def toString = "TranslatableComponent{translate=%s, with=%s, %s}".format(translate, replacements, super.toString())

  def setTranslate(translate: String) = this.translate = translate
  def getTranslate = this.translate
}
