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

package org.oac.gwaihir.aircraft.a320.system.elec

import org.scalatest.{Matchers, FlatSpec}

class BusTest extends FlatSpec with Matchers {

  "AC BUS 1" must "be energized when GEN 1 contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busOne.state should be (Bus.Unenergized)
    sys.ac.genOneContactor.close()
    exec.loop()
    sys.ac.busOne.state should be (Bus.Energized(ElectricalSystem.GenOneContId))
  }

  it must "be energized when BUS 1 TIE contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busOne.state should be (Bus.Unenergized)
    sys.ac.busOneTieContactor.close()
    exec.loop()
    sys.ac.busOne.state should be (Bus.Energized(ElectricalSystem.AcBusOneTieContId))
  }

  "AC BUS 2" must "be energized when GEN 2 contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busTwo.state should be (Bus.Unenergized)
    sys.ac.genTwoContactor.close()
    exec.loop()
    sys.ac.busTwo.state should be (Bus.Energized(ElectricalSystem.GenTwoContId))
  }

  it must "be energized when BUS 2 TIE contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busTwo.state should be (Bus.Unenergized)
    sys.ac.busTwoTieContactor.close()
    exec.loop()
    sys.ac.busTwo.state should be (Bus.Energized(ElectricalSystem.AcBusTwoTieContId))
  }

  "DC BUS 1" must "be energized when TR1 contactor is closed" in new ColdAndDarkSystem {
    sys.dc.busOne.state should be (Bus.Unenergized)
    sys.dc.trOneContactor.close()
    exec.loop()
    sys.dc.busOne.state should be (Bus.Energized(ElectricalSystem.TrOneContactorId))
  }

  "DC BUS 2" must "be energized when TR2 contactor is closed" in new ColdAndDarkSystem {
    sys.dc.busTwo.state should be (Bus.Unenergized)
    sys.dc.trTwoContactor.close()
    exec.loop()
    sys.dc.busTwo.state should be (Bus.Energized(ElectricalSystem.TrTwoContactorId))
  }
}
