package jk_5.nailed.server.command

import java.util

import jk_5.nailed.api.command.completion.CommandCompleter
import jk_5.nailed.api.command.context.CommandLocals
import jk_5.nailed.api.command.dispatcher.Dispatcher
import jk_5.nailed.api.command.parametric.binding.BindingBehavior
import jk_5.nailed.api.command.parametric.{ParameterData, ParametricCallable}
import jk_5.nailed.api.command.sender.{CommandSender, MapCommandSender}
import jk_5.nailed.server.NailedPlatform

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class NailedCommandCompleter extends CommandCompleter {

  var dispatcher: Dispatcher = _
  private val parameters = {
    val field = classOf[ParametricCallable].getDeclaredField("parameters")
    field.setAccessible(true)
    field
  }

  override def getSuggestions(arguments: String, locals: CommandLocals): java.util.List[String] = {
    val input = locals.get("CMD_INPUT").asInstanceOf[String]
    val inputPath = input.substring(0, input.length - arguments.length).trim
    val partialInput = input.substring(input.lastIndexOf(' ') + 1)
    val path = input.substring(0, input.lastIndexOf(' '))
    val out = new util.ArrayList[String]()
    val split = inputPath.split(" ", -1)
    val splitInput = arguments.split(" ", -1)
    val p = dispatcher.get(inputPath)

    var disp = dispatcher
    var break = false
    var paramData: Array[ParameterData] = Array.empty
    for(i <- 0 until split.length if !break){
      disp = disp.get(split(i)).getCallable match {
        case c: Dispatcher => c
        case c: ParametricCallable =>
          paramData = parameters.get(c).asInstanceOf[Array[ParameterData]]
          break = true
          disp
        case d =>
          break = true
          disp
      }
    }
    val realParams = paramData.filter(p => p.getBinding.getBehavior(p) != BindingBehavior.PROVIDES)
    val completingParam = if(realParams.length >= splitInput.length) realParams(splitInput.length - 1) else null
    if(completingParam == null) return out
    completingParam.getName match {
      case "gamemode" =>
        out.add("survival")
        out.add("creative")
        out.add("adventure")
        out.add("spectator")
      case "difficulty" =>
        out.add("peaceful")
        out.add("easy")
        out.add("medium")
        out.add("hard")
      case "weathertype" =>
        out.add("clear")
        out.add("dry")
        out.add("rain")
        out.add("thunder")
      case "mappack" =>
        NailedPlatform.getMappackRegistry.getAllIds.foreach(out.add)
      case "player" =>
        NailedPlatform.getOnlinePlayers.map(_.getName).foreach(out.add)
      case "team" =>
        val sender = locals.get(classOf[CommandSender])
        sender match {
          case s: MapCommandSender if s.getMap != null => s.getMap.getTeams.map(_.id()).foreach(out.add)
          case _ =>
        }
      case _ =>
    }
    val ret = new util.ArrayList[String]()
    out.filter(o => o.toLowerCase.startsWith(partialInput.toLowerCase)).foreach(o => ret.add(o))
    ret
  }
}
