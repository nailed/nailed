package jk_5.nailed.server.scheduler

import java.util
import java.util.concurrent
import java.util.concurrent.{Callable, TimeUnit}

import io.netty.util.concurrent._
import jk_5.eventbus.EventHandler
import jk_5.nailed.api.event.ServerPostTickEvent
import jk_5.nailed.api.scheduler.Scheduler

import scala.collection.mutable


/**
 * No description given
 *
 * @author jk-5
 */
object NailedScheduler extends Scheduler {
  val executor = new DefaultEventExecutorGroup(Runtime.getRuntime.availableProcessors() * 2)
  private val executionQueue = mutable.Queue[Runnable]()

  override def submit(task: Runnable): Future[_] = executor.submit(task)
  override def submit[T](task: Runnable, result: T): Future[T] = executor.submit(task, result)
  override def submit[T](task: Callable[T]): Future[T] = executor.submit(task)
  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] = executor.schedule(command, delay, unit)
  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] = executor.schedule(callable, delay, unit)
  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] = executor.scheduleAtFixedRate(command, initialDelay, period, unit)
  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] = executor.scheduleWithFixedDelay(command, initialDelay, delay, unit)
  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[concurrent.Future[T]] = executor.invokeAll(tasks)
  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[concurrent.Future[T]] = executor.invokeAll(tasks, timeout, unit)
  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T = executor.invokeAny(tasks)
  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = executor.invokeAny(tasks, timeout, unit)
  override def execute(command: Runnable): Unit = executor.execute(command)

  override def submitSync(task: Runnable) = this.submit(task, null)
  override def submitSync[T](task: Runnable, result: T): Future[T] = {
    val future = new DefaultPromise[T](this.executor.next())
    this.executionQueue += new Runnable {
      override def run(){
        try{
          task.run()
          future.setSuccess(result)
        }catch{
          case e: Exception => future.setFailure(e)
        }
      }
    }
    future
  }
  override def submitSync[T](task: Callable[T]): Future[T] = {
    val future = new DefaultPromise[T](this.executor.next())
    this.executionQueue += new Runnable {
      override def run(){
        try{
          future.setSuccess(task.call())
        }catch{
          case e: Exception => future.setFailure(e)
        }
      }
    }
    future
  }
  override def scheduleSync(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] = ???
  override def scheduleSync[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] = ???
  override def scheduleAtFixedRateSync(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] = ???
  override def scheduleWithFixedDelaySync(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] = ???
  override def invokeAllSync[T](tasks: util.Collection[_ <: Callable[T]]): util.List[concurrent.Future[T]] = ???
  override def invokeAllSync[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[concurrent.Future[T]] = ???
  override def invokeAnySync[T](tasks: util.Collection[_ <: Callable[T]]): T = ???
  override def invokeAnySync[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = ???
  override def executeSync(command: Runnable) = this.executionQueue += command

  @EventHandler
  def onTick(event: ServerPostTickEvent){
    if(executionQueue.size > 0){
      for(e <- executionQueue){
        e.run()
      }
      executionQueue.clear()
    }
  }
}