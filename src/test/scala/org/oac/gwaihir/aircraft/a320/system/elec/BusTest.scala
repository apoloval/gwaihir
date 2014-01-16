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
    sys.ac.busOne.state should be (Bus.DeEnergized)
    sys.ac.genOneContactor.close(GenOneId)
    exec.loop()
    sys.ac.busOne.state should be (Bus.Energized(GenOneId))
  }

  it must "be de-energized when GEN 1 contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.ac.genOneContactor.close(GenOneId)
    exec.loop()
    sys.ac.genOneContactor.open()
    exec.loop()
    sys.ac.busOne.state should be (Bus.DeEnergized)
  }

  it must "be energized when BUS 1 TIE contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busOne.state should be (Bus.DeEnergized)
    sys.ac.busOneTieContactor.close(ApuGenId)
    exec.loop()
    sys.ac.busOne.state should be (Bus.Energized(ApuGenId))
  }

  it must "be de-energized when BUS 1 TIE contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.ac.busOneTieContactor.close(ApuGenId)
    exec.loop()
    sys.ac.busOneTieContactor.open()
    exec.loop()
    sys.ac.busOne.state should be (Bus.DeEnergized)
  }

  "AC BUS 2" must "be energized when GEN 2 contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busTwo.state should be (Bus.DeEnergized)
    sys.ac.genTwoContactor.close(GenTwoId)
    exec.loop()
    sys.ac.busTwo.state should be (Bus.Energized(GenTwoId))
  }

  it must "be de-energized when GEN 2 contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.ac.genTwoContactor.close(GenTwoId)
    exec.loop()
    sys.ac.genTwoContactor.open()
    exec.loop()
    sys.ac.busTwo.state should be (Bus.DeEnergized)
  }

  it must "be energized when BUS 2 TIE contactor is closed" in new ColdAndDarkSystem {
    sys.ac.busTwo.state should be (Bus.DeEnergized)
    sys.ac.busTwoTieContactor.close(ExtPowerId)
    exec.loop()
    sys.ac.busTwo.state should be (Bus.Energized(ExtPowerId))
  }

  it must "be de-energized when BUS 2 TIE contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.ac.busTwoTieContactor.close(ApuGenId)
    exec.loop()
    sys.ac.busTwoTieContactor.open()
    exec.loop()
    sys.ac.busTwo.state should be (Bus.DeEnergized)
  }

  "DC BUS 1" must "be energized when TR1 contactor is closed" in new ColdAndDarkSystem {
    sys.dc.busOne.state should be (Bus.DeEnergized)
    sys.dc.trOneContactor.close(TrOneId)
    exec.loop()
    sys.dc.busOne.state should be (Bus.Energized(TrOneId))
  }

  it must "be de-energized when TR1 contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.dc.trOneContactor.close(TrOneId)
    exec.loop()
    sys.dc.trOneContactor.open()
    exec.loop()
    sys.dc.busOne.state should be (Bus.DeEnergized)
  }

  "DC BUS 2" must "be energized when TR2 contactor is closed" in new ColdAndDarkSystem {
    sys.dc.busTwo.state should be (Bus.DeEnergized)
    sys.dc.trTwoContactor.close(TrTwoId)
    exec.loop()
    sys.dc.busTwo.state should be (Bus.Energized(TrTwoId))
  }

  it must "be de-energized when TR2 contactor was closed and then opens" in new ColdAndDarkSystem {
    sys.dc.trTwoContactor.close(TrOneId)
    exec.loop()
    sys.dc.trTwoContactor.open()
    exec.loop()
    sys.dc.busTwo.state should be (Bus.DeEnergized)
  }

  "DC battery bus" must "be energized when DC TIE 1 contactor is closed" in new ColdAndDarkSystem {
    sys.dc.tieOneContactor.close(TrOneId)
    exec.loop()
    sys.dc.batteryBus.state should be (Bus.Energized(TrOneId))
  }

  it must "be energized when DC TIE 2 cont is closed and DC TIE 1 cont is open" in new ColdAndDarkSystem {
    sys.dc.tieTwoContactor.close(TrTwoId)
    exec.loop()
    sys.dc.batteryBus.state should be (Bus.Energized(TrTwoId))
  }

  it must "be energized by DC TIE 1 cont when both DC TIE conts are closed" in new ColdAndDarkSystem {
    sys.dc.tieOneContactor.close(TrOneId)
    sys.dc.tieTwoContactor.close(TrTwoId)
    exec.loop()
    sys.dc.batteryBus.state should be (Bus.Energized(TrOneId))
  }

  "Hot bus 1" must "be energized as soon as the battery one is initialized" in new ColdAndDarkSystem {
    sys.dc.hotBusOne.state should be (Bus.Energized(BatteryOneId))
  }

  "Hot bus 2" must "be energized as soon as the battery two is initialized" in new ColdAndDarkSystem {
    sys.dc.hotBusTwo.state should be (Bus.Energized(BatteryTwoId))
  }
}
