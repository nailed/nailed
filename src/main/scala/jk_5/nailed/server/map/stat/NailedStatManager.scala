package jk_5.nailed.server.map.stat

import jk_5.nailed.api.map.stat._
import jk_5.nailed.server.map.NailedMap

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class NailedStatManager(private val map: NailedMap) extends StatManager {

  private val stats = map.mappack match {
    case null => new Array[Stat](0)
    case m => m.getMetadata.stats.map{s =>
      if(s.track == null || s.track.isEmpty) new ModifiableStat(s.name) else new SubscribedStat(s.name, s.track, s.attributes)
    }.toArray
  }

  def fireEvent(event: StatEvent){
    stats.foreach{
      case s: SubscribedStat if s.track == event.name =>
        var stop = false
        for(e <- event.attributes.entrySet() if !stop){
          if(s.attributes.get(e.getKey) != e.getValue){
            stop = true
          }
        }
        s.onEvent(event)
      case _ =>
    }
  }

  def getStat(name: String): Stat = this.stats.find(_.name == name).orNull
}
