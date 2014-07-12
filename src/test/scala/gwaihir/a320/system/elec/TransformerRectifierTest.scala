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

package gwaihir.a320.system.elec

import org.scalatest.{WordSpec, Matchers}

class TransformerRectifierTest extends WordSpec with Matchers {

  "TR1" must {
    "be powered when AC BUS 1 is energized" in new ColdAndDarkSystem {
      sys.trOne.state should be (TransformerRectifier.Unpowered)
      sys.acBusOne.power(Seq(GenOneId))
      exec.loop()
      sys.trOne.state should be (TransformerRectifier.Powered(AcBusOneId, GenOneId))
    }
  }

  "TR2" must {
    "be powered when AC BUS 2 is energized" in new ColdAndDarkSystem {
      sys.trTwo.state should be (TransformerRectifier.Unpowered)
      sys.acBusTwo.power(Seq(ApuGenId))
      exec.loop()
      sys.trTwo.state should be (
        TransformerRectifier.Powered(AcBusTwoId, ApuGenId))
    }
  }

  "ESS TR" must {
    "be powered by AC ESS BUS when any TR is not operating" in new ColdAndDarkSystem {
      sys.essTr.state should be (TransformerRectifier.Unpowered)
      sys.acEssBus.power(Seq(AcBusOneId, GenOneId))
      exec.loop()
      sys.essTr.state should be (TransformerRectifier.Powered(AcEssBusId, AcBusOneId, GenOneId))
    }

    "be powered by EMER GEN when it is operating" in new ColdAndDarkSystem {
      sys.essTr.state should be (TransformerRectifier.Unpowered)
      sys.emerGen.powerOn()
      exec.loop()
      sys.essTr.state should be (TransformerRectifier.Powered(EmerGenId))
    }
  }
}
