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

import org.scalatest.{WordSpec, Matchers, FlatSpec}

class BusTest extends WordSpec with Matchers {

  "AC BUS 1" must {
    "be energized when GEN 1 is on" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.genOne.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.Energized(GenOneId))
    }

    "be energized by GEN 1 powered on before external power or APU gen" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      sys.genOne.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.Energized(GenOneId))
    }

    "be energized by external power or APU gen powered on before GEN 1" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.Energized(ExtPowerId))
    }

    "be energized by ext power/APU gen when GEN 1 is off and GEN 2 is on" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      sys.genTwo.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.Energized(ExtPowerId))
    }

    "be energized by GEN2 when there is no other power supply source" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.genTwo.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.Energized(GenTwoId))
    }

    "be de-energized when GEN 1 is off and other sources are on but BUS TIE button is off" in new ColdAndDarkSystem {
      sys.acBusOne.state should be (Bus.DeEnergized)
      sys.acBusTieSwitch.switch(AcBusTieSwitch.Off)
      sys.extPower.powerOn()
      sys.genTwo.powerOn()
      exec.loop()
      sys.acBusOne.state should be (Bus.DeEnergized)
    }
  }

  "AC BUS 2" must {
    "be energized when GEN 2 is on" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.genTwo.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.Energized(GenTwoId))
    }

    "be energized by GEN 2 powered on before external power or APU gen" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      sys.genTwo.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.Energized(GenTwoId))
    }

    "be energized by external power or APU gen powered on before GEN 2" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.Energized(ExtPowerId))
    }

    "be energized by ext power/APU gen when GEN 2 is off and GEN 1 is on" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.extPower.powerOn()
      sys.genOne.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.Energized(ExtPowerId))
    }

    "be energized by GEN1 when there is no other power supply source" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.genOne.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.Energized(GenOneId))
    }

    "be de-energized when GEN 2 is off and other sources are on but BUS TIE button is off" in new ColdAndDarkSystem {
      sys.acBusTwo.state should be (Bus.DeEnergized)
      sys.acBusTieSwitch.switch(AcBusTieSwitch.Off)
      sys.extPower.powerOn()
      sys.genOne.powerOn()
      exec.loop()
      sys.acBusTwo.state should be (Bus.DeEnergized)
    }
  }

  "AC ESS BUS" must {
    "be energized by a powered AC BUS 1 when AC ESS switch is NORM" in new ColdAndDarkSystem {
      sys.acEssBus.state should be (Bus.DeEnergized)
      sys.acBusOne.power(GenOneId)
      exec.loop()
      sys.acEssBus.state should be (Bus.Energized(AcBusOneId, GenOneId))
    }

    "be de-energized by a powered AC BUS 1 when AC ESS switch is ALT" in new ColdAndDarkSystem {
      sys.acEssBus.state should be (Bus.DeEnergized)
      sys.acEssFeedSwitch.switch(AcEssFeedSwitch.Alt)
      sys.acBusOne.power(GenOneId)
      exec.loop()
      sys.acEssBus.state should be (Bus.DeEnergized)
    }

    "be energized by a powered AC BUS 2 when AC ESS switch is ALT" in new ColdAndDarkSystem {
      sys.acEssBus.state should be (Bus.DeEnergized)
      sys.acEssFeedSwitch.switch(AcEssFeedSwitch.Alt)
      sys.acBusTwo.power(GenTwoId)
      exec.loop()
      sys.acEssBus.state should be (Bus.Energized(AcBusTwoId, GenTwoId))
    }

    "be de-energized by a powered AC BUS 2 when AC ESS switch is NORM" in new ColdAndDarkSystem {
      sys.acEssBus.state should be (Bus.DeEnergized)
      sys.acBusTwo.power(GenTwoId)
      exec.loop()
      sys.acEssBus.state should be (Bus.DeEnergized)
    }

    "be energized by emer gen when it is operating" in new ColdAndDarkSystem {
      sys.acEssBus.state should be (Bus.DeEnergized)
      sys.emerGen.powerOn()
      exec.loop()
      sys.acEssBus.state should be (Bus.Energized(EmerGenId))
    }
  }

  "DC BUS 1" must {
    "be energized when TR1 is powered" in new ColdAndDarkSystem {
      sys.dcBusOne.state should be (Bus.DeEnergized)
      sys.trOne.power(Seq(AcBusOneId, GenOneId))
      exec.loop()
      sys.dcBusOne.state should be (Bus.Energized(TrOneId))
    }

    "be energized by DC BAT BUS when TR1 is failing and TR2 is on" in new ColdAndDarkSystem {
      sys.dcBusOne.state should be (Bus.DeEnergized)
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      sys.dcBatBus.power(Seq(DcBusTwoId, TrTwoId))
      exec.loop()
      sys.dcBusOne.state should be (Bus.Energized(DcBatteryBusId, DcBusTwoId, TrTwoId))
    }
  }

  "DC BUS 2" must {
    "be energized when TR2 is powered" in new ColdAndDarkSystem {
      sys.dcBusTwo.state should be (Bus.DeEnergized)
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      exec.loop()
      sys.dcBusTwo.state should be (Bus.Energized(TrTwoId))
    }

    "be energized by DC BAT BUS when TR2 is failing and TR1 is on" in new ColdAndDarkSystem {
      sys.dcBusTwo.state should be (Bus.DeEnergized)
      sys.trOne.power(Seq(AcBusOneId, GenOneId))
      sys.dcBatBus.power(Seq(DcBusOneId, TrOneId))
      exec.loop()
      sys.dcBusTwo.state should be (Bus.Energized(DcBatteryBusId, DcBusOneId, TrOneId))
    }
  }

  "DC BAT BUS" must {
    "be energized by DC BUS 1 even when DC BUS 2 is powered" in new ColdAndDarkSystem {
      sys.dcBatBus.state should be (Bus.DeEnergized)
      sys.trOne.power(Seq(AcBusOneId, GenOneId))
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      sys.dcBusOne.power(TrOneId)
      sys.dcBusTwo.power(TrTwoId)
      exec.loop()
      sys.dcBatBus.state should be (Bus.Energized(DcBusOneId, TrOneId))
    }

    "be energized by DC BUS 2 when DC BUS 1 is unpowered" in new ColdAndDarkSystem {
      sys.dcBatBus.state should be (Bus.DeEnergized)
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      sys.dcBusTwo.power(TrTwoId)
      exec.loop()
      sys.dcBatBus.state should be (Bus.Energized(DcBusTwoId, TrTwoId))
    }
  }

  "DC ESS BUS" must {
    "be energized by DC BAT BUS when both TRs are operating" in new ColdAndDarkSystem {
      sys.dcEssBus.state should be (Bus.DeEnergized)
      sys.trOne.power(Seq(AcBusOneId, GenOneId))
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      exec.loop()
      sys.dcEssBus.state should be (Bus.Energized(DcBatteryBusId, DcBusOneId, TrOneId))
    }

    "be energized by ESS TR when any TR is not operating" in new ColdAndDarkSystem {
      sys.dcEssBus.state should be (Bus.DeEnergized)
      sys.acEssBus.power(Seq(AcBusTwoId, ApuGenId))
      sys.trTwo.power(Seq(AcBusTwoId, ApuGenId))
      sys.essTr.power(Seq(AcEssBusId, AcBusTwoId, ApuGenId))
      exec.loop()
      sys.dcEssBus.state should be (Bus.Energized(EssTrId))
    }
  }
}
