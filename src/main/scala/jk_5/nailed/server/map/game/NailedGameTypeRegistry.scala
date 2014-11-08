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
  private var typeCollection: java.util.Collection[GameType] = java.util.Arrays.asList()
  private val typesByName = mutable.HashMap[String, GameType]()

  override def registerGameType(gameType: GameType){
    types += gameType
    typeCollection = java.util.Arrays.asList(types.toArray: _*)
    typesByName.put(gameType.getName, gameType)
  }

  override def unregisterGameType(gameType: GameType){
    types -= gameType
    typeCollection = java.util.Arrays.asList(types.toArray: _*)
    typesByName.remove(gameType.getName)
  }

  override def getTypes = typeCollection

  override def getByName(name: String) = typesByName.get(name).orNull
}
