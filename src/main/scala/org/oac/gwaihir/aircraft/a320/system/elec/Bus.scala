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

import ElectricalSystem._

/** A trait providing matching conditions for events sent by buses. */
trait BusConditions {
  self: ConditionEvaluator =>

  /** A condition consistent of the given bus to be in the given state. */
  def busIs(busId: DeviceId, state: Bus.State) = deviceIs(busId, state)

  /** A condition consisting of the given bus to be energized. */
  def busIsEnergized(busId: DeviceId) = busIs(busId, Bus.Energized)

  /** A condition consisting of the given bus to be unenergized. */
  def busIsUnenergized(busId: DeviceId) = busIs(busId, Bus.Unenergized)
}

abstract class Bus(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Bus.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Bus._

  override val initialState = Bus.Unenergized

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  override def whenConditionIsMet = power()
  override def whenConditionIsNotMet = unpower()

  def power() = setState(Energized)
  def unpower() = setState(Unenergized)
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {
  val condition = contIsClosed(GenOneContId) or contIsClosed(BusTieContId)
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {
  val condition = contIsClosed(GenTwoContId) or contIsClosed(BusTieContId)
}

class DcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusOneId) {
  val condition = contIsClosed(TrOneContactorId)
}

class DcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusTwoId) {
  val condition = contIsClosed(TrTwoContactorId)
}

object Bus {

  sealed trait State
  case object Energized extends State
  case object Unenergized extends State
  val InitialState = Unenergized

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasEnergized = StateChangedEvent(Some(Unenergized), Energized)
  val WasUnenergized = StateChangedEvent(Some(Energized), Unenergized)
}