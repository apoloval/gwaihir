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

package org.oacsd.gwaihir.aircraft.a320.system.elec

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers
import org.oacsd.gwaihir.core.SimulationContext

class BusTest extends FlatSpec with MustMatchers {

  "AC BUS 1" must "be energized when GEN 1 contactor is closed" in new ElecSystemInitialized {
    sys.ac.busOne.state must be (Bus.Unenergized)
    sys.ac.genOneContactor.close()
    exec.loop()
    sys.ac.busOne.state must be (Bus.Energized)
  }

  it must "be energized when BUS TIE contactor is closed" in new ElecSystemInitialized {
    sys.ac.busOne.state must be (Bus.Unenergized)
    sys.ac.busTieContactor.close()
    exec.loop()
    sys.ac.busOne.state must be (Bus.Energized)
  }

  "AC BUS 2" must "be energized when GEN 2 contactor is closed" in new ElecSystemInitialized {
    sys.ac.busTwo.state must be (Bus.Unenergized)
    sys.ac.genTwoContactor.close()
    exec.loop()
    sys.ac.busTwo.state must be (Bus.Energized)
  }

  it must "be energized when BUS TIE contactor is closed" in new ElecSystemInitialized {
    sys.ac.busTwo.state must be (Bus.Unenergized)
    sys.ac.busTieContactor.close()
    exec.loop()
    sys.ac.busTwo.state must be (Bus.Energized)
  }

  trait ElecSystemInitialized {

    implicit val ctx = SimulationContext()
    val channel = ctx.eventChannel
    val exec = ctx.taskExecutor
    val sys = new ElectricalSystem()
    sys.init()
    exec.loop()
  }
}
