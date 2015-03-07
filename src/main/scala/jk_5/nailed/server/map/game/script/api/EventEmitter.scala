package jk_5.nailed.server.map.game.script.api

import jk_5.nailed.server.map.game.script.ScriptingEngine
import org.mozilla.javascript.Function

import scala.collection.mutable

trait EventEmitter {

  private[this] val handlers = mutable.HashMap[String, Array[Function]]()
  private[this] val singleHandlers = mutable.HashMap[String, Array[Function]]()

  private[api] val engine: ScriptingEngine

  def on(event: String, handler: Function){
    handlers.get(event) match {
      case Some(array) => handlers(event) = array :+ handler
      case None => handlers(event) = Array(handler)
    }
  }

  def once(event: String, handler: Function){
    singleHandlers.get(event) match {
      case Some(array) => singleHandlers(event) = array :+ handler
      case None => singleHandlers(event) = Array(handler)
    }
  }

  def remove(event: String, handler: Function){
    handlers.get(event) match {
      case Some(array) =>
        val newArray = array.filterNot(_ == handler)
        if(newArray.length == 0){
          handlers.remove(event)
        }else{
          handlers(event) = newArray
        }
      case None => //No handlers registered, do nothing
    }
    singleHandlers.get(event) match {
      case Some(array) =>
        val newArray = array.filterNot(_ == handler)
        if(newArray.length == 0){
          singleHandlers.remove(event)
        }else{
          singleHandlers(event) = newArray
        }
      case None => //No handlers registered, do nothing
    }
  }

  def emit(event: String, args: AnyRef*){
    engine.executor.execute(new Runnable {
      def run(){
        val argsArray = args.toArray
        handlers.get(event) match {
          case Some(array) => array.foreach(_.call(engine.context, engine.scope, engine.scope, argsArray))
          case None => //No handlers registered, do nothing
        }
        singleHandlers.get(event) match {
          case Some(array) =>
            array.foreach(_.call(engine.context, engine.scope, engine.scope, argsArray))
            singleHandlers.remove(event)
          case None => //No handlers registered, do nothing
        }
      }
    })
  }
}
