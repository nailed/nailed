package jk_5.nailed.plugins.internal.command

import jk_5.nailed.api.command._

/**
 * No description given
 *
 * @author jk-5
 */
object CommandStartGame extends Command("startgame") {

  override def execute(ctx: CommandContext, args: Arguments){
    val m = ctx.requireWorld().getMap
    if(m.isEmpty) throw ctx.error("There is no game in this world")
    val manager = m.get.getGameManager
    if(manager.isGameRunning) throw ctx.error("A game is already running")
    if(manager.startGame()){
      ctx.success("Started the game")
    }else{
      if(manager.hasCustomGameType){
        throw ctx.error("Could not start the game. An error has occurred in the GameType")
      }else{
        throw ctx.error("Could not start the game. No game.js was found")
      }
    }
  }
}
