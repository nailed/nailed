package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.command.{CommandSender, TabExecutor, WorldCommandSender}
import jk_5.nailed.api.player.Player
import jk_5.nailed.api.plugin.Command

/**
 * No description given
 *
 * @author jk-5
 */
object CommandTeam extends Command("team") with TabExecutor {

  override def execute(sender: CommandSender, args: Array[String]): Unit = sender match {
    case p: Player => if(args.length > 0) caseInsensitiveMatch(args(0)){
      case "join" =>
        if(args.length == 1){
          sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
        }else{
          val player = getTargetPlayer(sender, args(1))
          if(player.isEmpty) return
          if(args.length == 2){
            sender.sendMessage(new ComponentBuilder(s"Usage: /team join ${args(1)} <team>").color(ChatColor.RED).create())
          }else{
            val map = p.getMap
            map.getTeam(args(2)) match {
              case Some(team) =>
                if(args.length == 3){
                  map.setPlayerTeam(player.get, team)
                  val msg = new ComponentBuilder(s"Player ").color(ChatColor.GREEN).append(player.get.getName).append(" is now in team ").append(team.name).color(team.color).create()
                  map.broadcastChatMessage(msg)
                }else sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
              case None => sender.sendMessage(new ComponentBuilder("Unknown team").color(ChatColor.RED).create())
            }
          }
        }
      case _ => sender.sendMessage(new ComponentBuilder("Usage: /team join <username> <team>").color(ChatColor.RED).create())
    }
    case _ => sender.sendMessage(new ComponentBuilder("You are not a player").color(ChatColor.RED).create())
  }

  override def onTabComplete(sender: CommandSender, args: Array[String]): List[String] = args.length match {
    case 1 => getOptions(args, "join")
    case 2 => sender match {
      case s: WorldCommandSender => getUsernameOptions(args, s.getWorld.getMap.orNull)
      case s => List()
    }
    case 3 => sender match {
      case s: WorldCommandSender if s.getWorld.getMap.isDefined => getOptions(args, s.getWorld.getMap.get.getTeams.map(_.id))
      case s => List()
    }
    case _ => List()
  }
}
