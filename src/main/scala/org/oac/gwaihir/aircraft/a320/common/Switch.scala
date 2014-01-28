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

package org.oac.gwaihir.aircraft.a320.common

import org.oac.gwaihir.core._

trait SwitchConditions {
  self: ConditionEvaluator =>

  /** A condition consisting of the given switch to be on given state. */
  def switchIs(swId: DeviceId, state: Switch.State) = deviceIs(swId, state)

  /** A condition consisting of the given switch to be on. */
  def switchIsOn(swId: DeviceId): Condition[DeviceId] = deviceIs(swId, Switch.SwitchedOn)

  /** A condition consisting of the given switch to be off. */
  def switchIsOff(swId: DeviceId): Condition[DeviceId] = deviceIs(swId, Switch.SwitchedOff)
}

class Switch(val id: DeviceId)(implicit val ctx: SimulationContext)
      extends Device with StateMachine[Switch.State] {

  import Switch._

  override def initialState = SwitchedOff

  def switch(to: State) = setState(to)
}

object Switch {

  sealed trait State
  case object SwitchedOn extends State
  case object SwitchedOff extends State
  val InitialState = SwitchedOff

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasSwitchedOn = StateChangedEvent(Some(SwitchedOff), SwitchedOn)
  val WasSwitchedOff = StateChangedEvent(Some(SwitchedOn), SwitchedOff)
}
