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

import org.oac.gwaihir.core._
import org.oac.gwaihir.units.{Amperes, AmpereHours}

class Battery(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Battery.State] {

  var charge: AmpereHours = 23.0
  var flow: Amperes = 0.0

  def initialState = Battery.StandBy
}

class BatteryOne(implicit ctx: SimulationContext) extends Battery(ctx, BatteryOneId)

class BatteryTwo(implicit ctx: SimulationContext) extends Battery(ctx, BatteryTwoId)


object Battery {

  sealed trait State
  object StandBy extends State
  object Supplying extends State
  object Recharging extends State
}
