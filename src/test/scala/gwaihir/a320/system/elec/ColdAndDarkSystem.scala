/*
 *
 *  * This file is part of Gwaihir
 *  * Copyright (C) 2013, 2014 Alvaro Polo
 *  *
 *  * Gwaihir is free software: you can redistribute it and/or modify it under the terms of the GNU
 *  * General Public License as published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * Gwaihir is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along with Gwaihir. If not,
 *  * see <http://www.gnu.org/licenses/>.
 *
 */

package gwaihir.a320.system.elec

import gwaihir.core.SimulationContext

/** A fixture that provides a cold & dark electrical system. */
trait ColdAndDarkSystem {

  implicit val ctx = SimulationContext()
  val channel = ctx.eventChannel
  val exec = ctx.taskExecutor
  val sys = new ElectricalSystem()
  sys.init()
  exec.loop()
  sys.acBusTieSwitch.switch(AcBusTieSwitch.Auto)
  exec.loop()
}
