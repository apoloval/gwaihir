/*
 * This file is part of Open Airbus Cockpit
 * Copyright (C) 2012, 2013, 2014 Alvaro Polo
 *
 * Open Airbus Cockpit is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Open Airbus Cockpit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Open Airbus
 * Cockpit. If not, see <http://www.gnu.org/licenses/>.
 */

package org.oacsd.gwaihir.core

import scala.concurrent.duration.Duration
import scala.collection.mutable

/** An object able to execute tasks with form of Scala functions. */
trait TaskExecutor {

  /** Submit a new task to be executed. */
  def submit(task: => Unit)

  /** Schedule the given task to be executed after given duration. */
  def schedule(after: Duration)(task: => Unit)

  /** Loop on pending tasks and execute all of them.
    *
    * When there are no more pending tasks to be executed, the control is returned.
    */
  def loop()
}

object TaskExecutor {

  /** Create a new task executor from its default implementation. */
  def apply() = new DefaultTaskExecutor
}

class DefaultTaskExecutor extends TaskExecutor {

  private case class TaskInfo(task: () => Unit, triggerTime: Long)

  private implicit val ordering: Ordering[TaskInfo] =
    Ordering.by[TaskInfo, Long](t => t.triggerTime).reverse

  private val tasks: mutable.PriorityQueue[TaskInfo] = mutable.PriorityQueue.empty

  override def submit(task: => Unit) = tasks.enqueue(
    TaskInfo(() => task, System.currentTimeMillis())
  )

  override def schedule(after: Duration)(task: => Unit) = tasks.enqueue(
    TaskInfo(() => task, System.currentTimeMillis() + after.toMillis)
  )

  override def loop() {
    while (!tasks.isEmpty) {
      val taskInfo = tasks.dequeue()
      val timeToTrigger = taskInfo.triggerTime - System.currentTimeMillis()
      if (timeToTrigger > 0)
        Thread.sleep(timeToTrigger)
      taskInfo.task()
    }
  }
}
