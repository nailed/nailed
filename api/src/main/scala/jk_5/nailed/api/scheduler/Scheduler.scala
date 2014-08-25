/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.api.scheduler

import java.util
import java.util.concurrent
import java.util.concurrent.{Callable, TimeUnit}

import io.netty.util.concurrent.{Future, ScheduledFuture}

/**
 * No description given
 *
 * @author jk-5
 */
trait Scheduler {
  def submit(task: Runnable): Future[_]
  def submit[T](task: Runnable, result: T): Future[T]
  def submit[T](task: Callable[T]): Future[T]
  def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_]
  def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V]
  def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_]
  def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_]
  def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[concurrent.Future[T]]
  def invokeAll[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[concurrent.Future[T]]
  def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T
  def invokeAny[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T
  def execute(command: Runnable)

  def submitSync(task: Runnable): Future[_]
  def submitSync[T](task: Runnable, result: T): Future[T]
  def submitSync[T](task: Callable[T]): Future[T]
  def scheduleSync(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_]
  def scheduleSync[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V]
  def scheduleAtFixedRateSync(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_]
  def scheduleWithFixedDelaySync(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_]
  def invokeAllSync[T](tasks: util.Collection[_ <: Callable[T]]): util.List[concurrent.Future[T]]
  def invokeAllSync[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[concurrent.Future[T]]
  def invokeAnySync[T](tasks: util.Collection[_ <: Callable[T]]): T
  def invokeAnySync[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T
  def executeSync(command: Runnable)
}
