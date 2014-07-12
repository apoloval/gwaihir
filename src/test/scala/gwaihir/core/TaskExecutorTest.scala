/*
 * This file is part of Gwaihir
 * Copyright (C) 2013, 2014 Alvaro Polo
 *
 * Gwaihir is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Gwaihir is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Gwaihir. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package gwaihir.core

import scala.concurrent.duration._

import org.scalatest.{Matchers, FlatSpec}

class TaskExecutorTest extends FlatSpec with Matchers {

  "Task executor" must "execute a submitted task after loop" in new ExecutorInitialized {
    var taskDone = false
    exec.submit { taskDone = true }
    taskDone should be (false)
    exec.loop()
    taskDone should be (true)
  }

  it must "execute scheduled task on time" in new ExecutorInitialized {
    val submittedOn = System.currentTimeMillis()
    var execOn = 0l
    exec.schedule(100.milliseconds) { execOn = System.currentTimeMillis() }
    exec.loop()
    (execOn - submittedOn) should be >= 100l
  }

  it must "execute immediate task before one scheduled on future" in new ExecutorInitialized {
    var submittedOn = System.currentTimeMillis()
    var immediateDoneOn = 0l
    var scheduledDoneOn = 0l
    exec.schedule(100.milliseconds) { scheduledDoneOn = System.currentTimeMillis() }
    exec.submit { immediateDoneOn = System.currentTimeMillis() }
    exec.loop()
    (immediateDoneOn - submittedOn) should be < 10l
    (scheduledDoneOn - submittedOn) should be >= 100l
  }

  it must "support events submitted from tasks" in new ExecutorInitialized {
    var i = 0
    var doneOn = 0l
    def incToTen {
      if (i < 10) {
        i = i + 1
        exec.schedule(50.milliseconds)(incToTen)
      } else {
        doneOn = System.currentTimeMillis()
      }
    }
    var submittedOn = System.currentTimeMillis()
    exec.schedule(50.milliseconds)(incToTen)
    exec.loop()
    i should be (10)
    (doneOn - submittedOn) should be >= 500l
  }

  trait ExecutorInitialized {
    var exec = TaskExecutor()
  }
}
