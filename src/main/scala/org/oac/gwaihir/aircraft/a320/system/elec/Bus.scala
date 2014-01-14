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
  def busIsEnergized(busId: DeviceId) = deviceIs[Bus.State](busId) {
    case Bus.Energized(_) => true
    case _ => false
  }

  /** A condition consisting of the given bus to be energized by given supplier. */
  def busIsEnergizedBy(busId: DeviceId, supplierId: DeviceId) =
    busIs(busId, Bus.Energized(supplierId))

  /** A condition consisting of the given bus to be unenergized. */
  def busIsUnenergized(busId: DeviceId) = busIs(busId, Bus.Unenergized)
}

abstract class Bus(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Bus.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Bus._

  override def initialState = Bus.Unenergized

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  def power(by: DeviceId) = setState(Energized(by))
  def unpower() = setState(Unenergized)

  protected def watchPoweredByContactor(contId: DeviceId) = watch(contIsClosed(contId)) {
    power(contId)
  }

  protected def watchUnpoweredByContactors(contIds: DeviceId*) = {
    val allContsAreOpen = contIds.foldLeft[Condition](TrueCondition) {
      (c, cont) => c and contIsOpen(cont)
    }
    watch(allContsAreOpen) { unpower() }
  }

  /** Watch that the bus is powered by any of the given contactors.
    *
    * If any of these contactors is closed, the bus determines it is powered by that contactor.
    * If all of them are open, the bus determines it is unpowered.
    */
  protected def watchPoweredByContactors(contIds: DeviceId*) = {
    contIds.foreach(cont => watchPoweredByContactor(cont))
    watchUnpoweredByContactors(contIds: _*)
  }
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {

  watchPoweredByContactor(GenOneContId)
  watch(contIsClosed(AcBusOneTieContId) and contIsOpen(GenOneContId)) { power(AcBusOneTieContId) }
  watchUnpoweredByContactors(GenOneContId, AcBusOneTieContId)
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {

  watchPoweredByContactor(GenTwoContId)
  watch(contIsClosed(AcBusTwoTieContId) and contIsOpen(GenTwoContId)) { power(AcBusTwoTieContId) }
  watchUnpoweredByContactors(GenTwoContId, AcBusTwoTieContId)
}

class DcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusOneId) {

  watchPoweredByContactors(TrOneContactorId)
}

class DcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusTwoId) {

  watchPoweredByContactors(TrTwoContactorId)
}

object Bus {

  sealed trait State
  case class Energized(by: DeviceId) extends State
  case object Unenergized extends State
  val InitialState = Unenergized

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasEnergized = StateChangedEvent(Some(Unenergized), Energized)
  val WasUnenergized = StateChangedEvent(Some(Energized), Unenergized)
}