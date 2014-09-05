package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.command._
import jk_5.nailed.api.world.Difficulty

/**
 * No description given
 *
 * @author jk-5
 */
object CommandDifficulty extends Command("difficulty") with TabExecutor {

  override def execute(ctx: CommandContext, args: Arguments){
    val world = ctx.requireWorld()
    var d = Difficulty.byName(args.getString(0, "difficulty").toLowerCase)
    if(d == null){
      d = Difficulty.byId(args.getInt(0, 0, 3))
      if(d == null) throw ctx.error(s"Unknown difficulty ${args.getString(0, "difficulty")}")
    }
    world.setDifficulty(d)
    ctx.success(s"Set difficulty to ${d.getName}")
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => autocomplete(args, "peaceful", "easy", "normal", "hard")
    case _ => List()
  }
}
