package jk_5.nailed.plugin.internal.command

import java.text.DecimalFormat

import jk_5.nailed.api.chat.{BaseComponent, ChatColor, ComponentBuilder, TabExecutor}
import jk_5.nailed.api.command.CommandSender
import jk_5.nailed.api.plugin.Command
import jk_5.nailed.server.world.NailedDimensionManager
import net.minecraft.server.MinecraftServer

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTps extends Command("tps") with TabExecutor {

  final val timeFormatter = new DecimalFormat("########0.000")

  override def execute(sender: CommandSender, args: Array[String]){
    val server = MinecraftServer.getServer
    val meanTime = mean(server.tickTimeArray) * 1.0E-6D
    val meanTps = Math.min(1000.0 / meanTime, 20)
    sender.sendMessage(this.getComponent("Overall", meanTime, meanTps))
    for(dim <- NailedDimensionManager.getAllDimensionIds){
      val worldTickTime = mean(server.worldTickTimes.get(dim)) * 1.0E-6D
      val worldTPS = Math.min(1000.0 / worldTickTime, 20)
      sender.sendMessage(this.getComponent("Dim " + dim, worldTickTime, worldTPS))
    }
  }

  private def getComponent(prefix: String, tickTime: Double, tps: Double): BaseComponent = {
    val builder = new ComponentBuilder(prefix + ": ")
    builder.append("TPS: " + timeFormatter.format(tps))
    if(tps != 20) builder.color(ChatColor.red)
    builder.append(" Tick Time: " + timeFormatter.format(tickTime) + "ms")
    if(tickTime > 45) builder.color(ChatColor.red) else if(tickTime > 35) builder.color(ChatColor.gold)
    val percent = (tps / 20) * 100
    builder.append(" (" + timeFormatter.format(percent) + "%)")
    builder.createFlat()
  }

  private def mean(values: Array[Long]) = {
    var sum = 0L
    for(v <- values) sum += v
    sum / values.length
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = List()
}
