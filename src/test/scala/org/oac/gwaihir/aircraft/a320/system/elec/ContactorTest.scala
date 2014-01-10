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

import org.scalatest.FlatSpec
import org.scalatest.matchers.MustMatchers

class ContactorTest extends FlatSpec with MustMatchers {


  "GEN 1 contactor" must "close when GEN 1 is on" in new ColdAndDarkSystem {
    sys.ac.genOneContactor.state must be (Contactor.Open)
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.genOneContactor.state must be (Contactor.Closed)
  }

  it must "remain close even with EXT power and APU GEN on" in new ColdAndDarkSystem {
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.genOneContactor.state must be (Contactor.Closed)
    sys.ac.extPower.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genOneContactor.state must be (Contactor.Closed)
  }

  "GEN 2 contactor" must "close when GEN 2 is on" in new ColdAndDarkSystem {
    sys.ac.genTwoContactor.state must be (Contactor.Open)
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.genTwoContactor.state must be (Contactor.Closed)
  }

  it must "remain close even with EXT power and APU GEN on" in new ColdAndDarkSystem {
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.genTwoContactor.state must be (Contactor.Closed)
    sys.ac.extPower.powerOn()
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.genTwoContactor.state must be (Contactor.Closed)
  }

  "APU GEN contactor" must "close when no other generator is powered on" in new ColdAndDarkSystem {
    sys.ac.apuGenContactor.state must be (Contactor.Open)
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state must be (Contactor.Closed)
  }

  it must "open when other source is present" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state must be (Contactor.Closed)
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state must be (Contactor.Open)
  }

  it must "open when APU GEN is powered off" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.apuGenContactor.state must be (Contactor.Closed)
    sys.ac.apuGen.powerOff()
    exec.loop()
    sys.ac.apuGenContactor.state must be (Contactor.Open)
  }

  "EXT PWR contactor" must "close when EXT power is on and GEN 1 & 2 are off" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state must be (Contactor.Closed)
  }

  it must "remain close when APU GEN is connected" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state must be (Contactor.Closed)
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state must be (Contactor.Closed)
  }

  it must "open when one GEN is powered on" in new ColdAndDarkSystem {
    sys.ac.extPower.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state must be (Contactor.Closed)
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.extPowerContactor.state must be (Contactor.Open)
  }

  "Bus tie contactor" must "stay open when no power is available" in new ColdAndDarkSystem {
    sys.ac.busTieContactor.state must be (Contactor.Open)
  }

  it must "close when any GEN is powered off" in new ColdAndDarkSystem {
    sys.ac.busTieContactor.state must be (Contactor.Open)
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.busTieContactor.state must be (Contactor.Closed)
  }

  it must "open when both GEN 1 & 2 are powered on" in new ColdAndDarkSystem {
    sys.ac.genOne.powerOn()
    exec.loop()
    sys.ac.busTieContactor.state must be (Contactor.Closed)
    sys.ac.genTwo.powerOn()
    exec.loop()
    sys.ac.busTieContactor.state must be (Contactor.Open)
  }

  it must "close when APU gen or EXT PWR are on" in new ColdAndDarkSystem {
    sys.ac.apuGen.powerOn()
    exec.loop()
    sys.ac.busTieContactor.state must be (Contactor.Closed)
  }

  "AC ESS feed tie normal contactor" must "close when AC BUS 1 is energized and switch is NORM" in new ColdAndDarkSystem {
    sys.ac.acEssFeedNormContactor.state must be (Contactor.Open)
    sys.ac.busOne.power()
    exec.loop()
    sys.ac.acEssFeedNormContactor.state must be (Contactor.Closed)
  }

  it must "remain open when AC BUS 1 is energized but switch is ALT" in new ColdAndDarkSystem {
    sys.ac.acEssFeedNormContactor.state must be (Contactor.Open)
    sys.ac.busOne.power()
    sys.panel.acEssFeedSwitch.switchOn()
    exec.loop()
    sys.ac.acEssFeedNormContactor.state must be (Contactor.Open)
  }
}
