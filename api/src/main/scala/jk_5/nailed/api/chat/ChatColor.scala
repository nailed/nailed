package jk_5.nailed.api.chat

import java.util.regex.Pattern

import scala.collection.immutable

/**
 * No description given
 *
 * @author jk-5
 */
object ChatColor {
  final val black = new ChatColor('0', "black")
  final val darkBlue = new ChatColor('0', "black")
  final val darkGreen = new ChatColor('2', "dark_green")
  final val darkAqua = new ChatColor('3', "dark_aqua")
  final val darkRed = new ChatColor('4', "dark_red")
  final val darkPurple = new ChatColor('5', "dark_purple")
  final val gold = new ChatColor('6', "gold")
  final val gray = new ChatColor('7', "gray")
  final val darkGray = new ChatColor('8', "dark_gray")
  final val blue = new ChatColor('9', "blue")
  final val green = new ChatColor('a', "green")
  final val aqua = new ChatColor('b', "aqua")
  final val red = new ChatColor('c', "red")
  final val lightPurple = new ChatColor('d', "light_purple")
  final val yellow = new ChatColor('e', "yellow")
  final val white = new ChatColor('f', "white")
  final val magic = new ChatColor('k', "obfuscated")
  final val bold = new ChatColor('l', "bold")
  final val strikethrough = new ChatColor('m', "strikethrough")
  final val underline = new ChatColor('n', "underline")
  final val italic = new ChatColor('o', "italic")
  final val reset = new ChatColor('r', "reset")

  final val colorChar: Char = '\u00A7'
  final val allCodes = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr"
  final val stripColorPattern = Pattern.compile("(?i)" + String.valueOf(this.colorChar) + "[0-9A-FK-OR]")
  final val allChars = immutable.HashMap[Char, ChatColor](
    '0' -> ChatColor.black,
    '1' -> ChatColor.darkBlue,
    '2' -> ChatColor.darkGreen,
    '3' -> ChatColor.darkAqua,
    '4' -> ChatColor.darkRed,
    '5' -> ChatColor.darkPurple,
    '6' -> ChatColor.gold,
    '7' -> ChatColor.gray,
    '8' -> ChatColor.darkGray,
    '9' -> ChatColor.blue,
    'a' -> ChatColor.green,
    'b' -> ChatColor.aqua,
    'c' -> ChatColor.red,
    'd' -> ChatColor.lightPurple,
    'e' -> ChatColor.yellow,
    'f' -> ChatColor.white,
    'k' -> ChatColor.magic,
    'l' -> ChatColor.bold,
    'm' -> ChatColor.strikethrough,
    'n' -> ChatColor.underline,
    'o' -> ChatColor.italic,
    'r' -> ChatColor.reset
  )

  def stripColor(input: String) = if(input == null) null else this.stripColorPattern.matcher(input).replaceAll("")
  def translateAlternateColorCodes(altColorChar: Char, textToTranslate: String): String = {
    val b = textToTranslate.toCharArray
    for(i <- 0 until b.length - 1){
      if(b(i) == altColorChar && allCodes.indexOf(b(i + 1)) > -1){
        b(i) = ChatColor.colorChar
        b(i + 1) = Character.toLowerCase(b(i + 1))
      }
    }
    new String(b)
  }
  def getByChar(c: Char): ChatColor = allChars.get(c).orNull
}

case class ChatColor(code: Char, name: String){
  override val toString = new StringBuilder().append(ChatColor.colorChar).append(code).toString()
  def getName = this.name
}
