package jk_5.nailed.worldedit

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.event.platform.{CommandEvent, CommandSuggestionEvent}
import com.sk89q.worldedit.util.command.CommandMapping
import jk_5.nailed.api.command._
import jk_5.nailed.api.player.Player

import scala.collection.JavaConverters._

/**
 * No description given
 *
 * @author jk-5
 */
class WorldEditCommand(val c: CommandMapping) extends Command(c.getPrimaryAlias, c.getAllAliases: _*) with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val p = ctx.requirePlayer()
    val split = new Array[String](args.amount + 1)
    split(0) = c.getPrimaryAlias
    System.arraycopy(args.arguments, 0, split, 1, args.amount)
    val weEvent = new CommandEvent(WorldEditPlayer.wrap(p), split.mkString(" "))
    WorldEdit.getInstance.getEventBus.post(weEvent)
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = sender match {
    case p: Player =>
      val split = new Array[String](args.length + 1)
      split(0) = c.getPrimaryAlias
      System.arraycopy(args, 0, split, 1, args.length)
      val event = new CommandSuggestionEvent(WorldEditPlayer.wrap(p), split.mkString(" "))
      WorldEdit.getInstance.getEventBus.post(event)
      event.getSuggestions.asScala.toList
    case _ => List()
  }
}
