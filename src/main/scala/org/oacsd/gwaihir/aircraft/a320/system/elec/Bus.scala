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

package org.oacsd.gwaihir.aircraft.a320.system.elec

import org.oacsd.gwaihir.core._

import ElectricalSystem._

/** A trait providing matching conditions for events sent by buses. */
trait BusConditions {
  self: ConditionEvaluator =>

  /** A condition consistent of the given bus to be in the given state. */
  def busIs(busId: DeviceId, state: Bus.State) = eventMatch(busId, {
    case Bus.StateChangedEvent(_, `state`) => true
    case _ => false
  })

  /** A condition consisting of the given bus to be energized. */
  def busIsEnergized(busId: DeviceId) = busIs(busId, Bus.Energized)

  /** A condition consisting of the given bus to be unenergized. */
  def busIsUnenergized(busId: DeviceId) = busIs(busId, Bus.Unenergized)
}

abstract class Bus(ctx: SimulationContext, val id: DeviceId)
    extends Device with ConditionEvaluator with ContactorConditions {

  import Bus._

  override val channel: EventChannel = ctx.eventChannel

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  override def whenConditionIsMet = power()
  override def whenConditionIsNotMet = unpower()

  var _state: State = InitialState
  def state = _state

  def power() = _state match {
    case Energized =>
    case Unenergized =>
      _state = Energized
      ctx.eventChannel.send(id, WasEnergized)
  }

  def unpower() = _state match {
    case Energized =>
      _state = Unenergized
      ctx.eventChannel.send(id, WasUnenergized)
    case Unenergized =>
  }
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {
  val condition = contIsClosed(GenOneContId) or contIsClosed(BusTieContId)
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {
  val condition = contIsClosed(GenTwoContId) or contIsClosed(BusTieContId)
}

object Bus {

  sealed trait State
  case object Energized extends State
  case object Unenergized extends State
  val InitialState = Unenergized

  case class StateChangedEvent(oldState: Option[State], newState: State)
  object WasInitialized extends StateChangedEvent(None, InitialState)
  object WasEnergized extends StateChangedEvent(Some(Unenergized), Energized)
  object WasUnenergized extends StateChangedEvent(Some(Energized), Unenergized)
}