/*
 * This file is part of Open Airbus Cockpit
 * Copyright (C) 2012, 2013, 2014 Alvaro Polo
 *
 * Open Airbus Cockpit is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Open Airbus Cockpit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Open Airbus
 * Cockpit. If not, see <http://www.gnu.org/licenses/>.
 */

package org.oacsd.gwaihir.aircraft.a320.common

import org.oacsd.gwaihir.core.{ConditionEvaluator, SimulationContext, DeviceId, Device}

trait SwitchConditions {
  self: ConditionEvaluator =>

  /** A condition consisting of the given switch to be in the given state. */
  def switchIs(swId: DeviceId, state: Switch.State) = eventMatch(swId, {
    case Switch.StateChangedEvent(_, `state`) => true
    case _ => false
  })

  /** A condition consisting of the given switch to be on. */
  def switchIsOn(swId: DeviceId) = switchIs(swId, Switch.SwitchedOn)

  /** A condition consisting of the given switch to be off. */
  def switchIsOff(swId: DeviceId) = switchIs(swId, Switch.SwitchedOff)
}

class Switch(val id: DeviceId)(implicit ctx: SimulationContext) extends Device {

  import Switch._

  var _state: State = SwitchedOff

  def switchOn() = _state match {
    case SwitchedOn =>
    case SwitchedOff =>
      _state = SwitchedOn
      ctx.eventChannel.send(id, WasSwitchedOn)
  }

  def switchOff() = _state match {
    case SwitchedOn =>
      _state = SwitchedOff
      ctx.eventChannel.send(id, WasSwitchedOff)
    case SwitchedOff =>
  }

  /** Initialize the device. */
  def init() = ctx.eventChannel.send(id, WasInitialized)
}

object Switch {

  sealed trait State
  case object SwitchedOn extends State
  case object SwitchedOff extends State
  val InitialState = SwitchedOff

  case class StateChangedEvent(oldState: Option[State], newState: State)
  object WasInitialized extends StateChangedEvent(None, InitialState)
  object WasSwitchedOn extends StateChangedEvent(Some(SwitchedOff), SwitchedOn)
  object WasSwitchedOff extends StateChangedEvent(Some(SwitchedOn), SwitchedOff)
}
