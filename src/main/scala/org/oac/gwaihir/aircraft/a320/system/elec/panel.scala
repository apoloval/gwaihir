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

import org.oac.gwaihir.aircraft.a320.common.Switch
import org.oac.gwaihir.core.SimulationContext

class AcEssFeedButton(implicit val _ctx: SimulationContext) extends Switch(AcEssFeedButtonId)(_ctx)

object AcEssFeedButton {

  val Norm = Switch.SwitchedOff
  val Alt = Switch.SwitchedOn
}

class AcBusTieButton(implicit val _ctx: SimulationContext) extends Switch(AcBusTieButtonId)(_ctx)

object AcBusTieButton {

  val Off = Switch.SwitchedOff
  val Auto = Switch.SwitchedOn
}
