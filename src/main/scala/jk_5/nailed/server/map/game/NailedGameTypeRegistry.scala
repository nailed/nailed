package jk_5.nailed.server.map.game

import jk_5.nailed.api.map.{GameType, GameTypeRegistry}

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NailedGameTypeRegistry extends GameTypeRegistry {

  private val types = mutable.ArrayBuffer[GameType]()
  private var typeArray = new Array[GameType](0)
  private val typesByName = mutable.HashMap[String, GameType]()

  override def registerGameType(gameType: GameType){
    types += gameType
    typeArray = types.toArray
    typesByName.put(gameType.getName, gameType)
  }

  override def unregisterGameType(gameType: GameType){
    types -= gameType
    typeArray = types.toArray
    typesByName.remove(gameType.getName)
  }

  override def getTypes = typeArray

  override def getByName(name: String) = typesByName.get(name).orNull
}
