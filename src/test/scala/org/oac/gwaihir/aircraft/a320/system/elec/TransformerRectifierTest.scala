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

class TransformerRectifierTest extends FlatSpec with Matchers {

  "TR1" must "be powered when AC BUS 1 is energized" in new ColdAndDarkSystem {
    sys.ac.trOne.state should be (TransformerRectifier.Unpowered)
    sys.ac.busOne.power(Seq(GenOneContId, GenOneId))
    exec.loop()
    sys.ac.trOne.state should be (TransformerRectifier.Powered(AcBusOneId, GenOneContId, GenOneId))
  }

  "TR2" must "be powered when AC BUS 2 is energized" in new ColdAndDarkSystem {
    sys.ac.trTwo.state should be (TransformerRectifier.Unpowered)
    sys.ac.busTwo.power(Seq(AcBusTwoTieContId, ApuGenContId, ApuGenId))
    exec.loop()
    sys.ac.trTwo.state should be (
      TransformerRectifier.Powered(AcBusTwoId, AcBusTwoTieContId, ApuGenContId, ApuGenId))
  }
}
