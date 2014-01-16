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

/** A trait providing matching conditions for events sent by buses. */
trait BusConditions {
  self: ConditionEvaluator =>

  /** A condition consistent of the given bus to be in the given state. */
  def busIs(busId: DeviceId, state: Bus.State): BooleanCondition  = deviceIs(busId, state)

  /** A condition consisting of the given bus to be energized. */
  def busIsEnergized(busId: DeviceId): BooleanCondition  = deviceIs[Bus.State](busId) {
    case Bus.Energized(_) => true
    case _ => false
  }

  /** A condition consisting of the given bus to be energized by given supplier. */
  def busIsEnergizedBy(busId: DeviceId): Condition[DeviceId] =
    deviceStateChanged[Bus.State, DeviceId](busId) {
      case Bus.Energized(dev) => Some(dev)
      case _ => None
    }

  /** A condition consisting of the given bus to be unenergized. */
  def busIsUnenergized(busId: DeviceId): BooleanCondition  = busIs(busId, Bus.DeEnergized)
}

abstract class Bus(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Bus.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Bus._

  override def initialState = Bus.DeEnergized

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  def power(by: DeviceId) = setState(Energized(by))
  def unpower() = setState(DeEnergized)

  protected def watchPoweredByContactor(contId: DeviceId) =
    watch(contIsClosedBy(contId)) { supplier => power(supplier) }

  protected def watchUnpoweredByContactors(contIds: DeviceId*) = {
    val allContsAreOpen = contIds.foldLeft[BooleanCondition](TrueCondition) {
      (c, cont) => c and contIsOpen(cont)
    }
    watch(allContsAreOpen) { areOpen => if (areOpen) { unpower() } }
  }
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {

  watchPoweredByContactor(GenOneContId)
  watchPoweredByContactor(AcBusOneTieContId)
  watchUnpoweredByContactors(GenOneContId, AcBusOneTieContId)
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {

  watchPoweredByContactor(GenTwoContId)
  watchPoweredByContactor(AcBusTwoTieContId)
  watchUnpoweredByContactors(GenTwoContId, AcBusTwoTieContId)
}

class DcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusOneId) {

  watch(contIsClosed(TrOneContactorId)) {
    case true => power(TrOneId)
    case false => unpower()
  }
}

class DcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusTwoId) {

  watch(contIsClosed(TrTwoContactorId)) {
    case true => power(TrTwoId)
    case false => unpower()
  }
}

object Bus {

  sealed trait State
  case class Energized(by: DeviceId) extends State
  case object DeEnergized extends State
  val InitialState = DeEnergized

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasEnergized = StateChangedEvent(Some(DeEnergized), Energized)
  val WasUnenergized = StateChangedEvent(Some(Energized), DeEnergized)
}