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

class ContactorTest extends FlatSpec with Matchers {

  "GEN 1 contactor" must "close when GEN 1 is on" in new ColdAndDarkSystem {
    sys.ac.genOneContactor.state should be (Contactor.Open)
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.genOneContactor.state should be (Contactor.Closed)
  }

  it must "remain close even with EXT power and APU GEN on" in new ColdAndDarkSystem {
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.extPower.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genOneContactor.state should be (Contactor.Closed)
  }

  "GEN 2 contactor" must "close when GEN 2 is on" in new ColdAndDarkSystem {
    sys.ac.genTwoContactor.state should be (Contactor.Open)
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.genTwoContactor.state should be (Contactor.Closed)
  }

  it must "remain close even with EXT power and APU GEN on" in new ColdAndDarkSystem {
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.extPower.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genTwoContactor.state should be (Contactor.Closed)
  }

  "APU GEN contactor" must "close when only APU gen is powered on and others are off" in new ColdAndDarkSystem {
    sys.ac.apuGenContactor.state should be (Contactor.Open)
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state should be (Contactor.Closed)
  }

  it must "open when APU GEN is powered off" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.apuGen.powerOff()
    exec.loop()
    sys.ac.apuGenContactor.state should be (Contactor.Open)
  }

  it must "open when external power is connected" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state should be (Contactor.Open)
  }

  it must "open when both GENs are powered on" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genOne.powerOn()
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state should be (Contactor.Open)
  }

  it must "remain close when one GEN is powered on" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state should be (Contactor.Closed)
  }

  "EXT PWR contactor" must "close when EXT power is on and GEN 1 & 2 are off" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state should be (Contactor.Closed)
  }

  it must "remain close when APU GEN is connected" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state should be (Contactor.Closed)
  }

  it must "remain close when one GEN is connected" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state should be (Contactor.Closed)
  }

  it must "open when both GENs are powered on" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.genOne.powerOn()
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state should be (Contactor.Open)
  }

  "AC Bus 1 tie contactor" must "stay open when no power is available" in new ColdAndDarkSystem {
    sys.ac.busOneTieContactor.state should be (Contactor.Open)
  }

  it must "close when GEN 1 is on but all others are off" in new ColdAndDarkSystem {
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.busOneTieContactor.state should be (Contactor.Closed)
  }

  it must "remain open when GEN 1 is on but some other is on" in new ColdAndDarkSystem {
    sys.ac.genOne.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.busOneTieContactor.state should be (Contactor.Open)
  }

  it must "close when GEN 1 is off but some other is on" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.busOneTieContactor.state should be (Contactor.Closed)
  }

  "AC Bus 2 tie contactor" must "stay open when no power is available" in new ColdAndDarkSystem {
    sys.ac.busTwoTieContactor.state should be (Contactor.Open)
  }

  it must "close when GEN 2 is on but all others are off" in new ColdAndDarkSystem {
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.busTwoTieContactor.state should be (Contactor.Closed)
  }

  it must "remain open when GEN 2 is on but some other is on" in new ColdAndDarkSystem {
    sys.ac.genTwo.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.busTwoTieContactor.state should be (Contactor.Open)
  }

  it must "close when GEN 2 is off but some other is on" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.busTwoTieContactor.state should be (Contactor.Closed)
  }

  "AC ESS feed tie normal contactor" must "close when AC BUS 1 is energized and switch is NORM" in new ColdAndDarkSystem {
    sys.ac.acEssFeedNormContactor.state should be (Contactor.Open)
    sys.ac.busOne.power(ElectricalSystem.GenOneContId)
    exec.loop()
    sys.ac.acEssFeedNormContactor.state should be (Contactor.Closed)
  }

  it must "remain open when AC BUS 1 is energized but switch is ALT" in new ColdAndDarkSystem {
    sys.ac.busOne.power(ElectricalSystem.GenOneContId)
    sys.panel.acEssFeedSwitch.switchOn()
    exec.loop()
    sys.ac.acEssFeedNormContactor.state should be (Contactor.Open)
  }

  "TR1 contactor" must "close when TR1 is operating" in new ColdAndDarkSystem {
    sys.ac.trOne.power()
    exec.loop()
    sys.dc.trOneContactor.state should be (Contactor.Closed)
  }

  "TR2 contactor" must "close when TR2 is operating" in new ColdAndDarkSystem {
    sys.ac.trTwo.power()
    exec.loop()
    sys.dc.trTwoContactor.state should be (Contactor.Closed)
  }

  "DC Bus tie 1 contactor" must "close when DC BUS 1 is energized" in new ColdAndDarkSystem {
    sys.dc.busOne.power(ElectricalSystem.TrOneContactorId)
    exec.loop()
    sys.dc.tieOneContactor.state should be (Contactor.Closed)
  }

  it must "close when DC BUS 2 is energized regardless DC BUS 1 state" in new ColdAndDarkSystem {
    sys.dc.busTwo.power(ElectricalSystem.TrOneContactorId)
    exec.loop()
    sys.dc.tieOneContactor.state should be (Contactor.Closed)
  }
}
