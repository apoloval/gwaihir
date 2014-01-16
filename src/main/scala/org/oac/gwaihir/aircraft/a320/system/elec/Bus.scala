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
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {

  watch(contIsClosedBy(GenOneContId), contIsClosedBy(AcBusOneTieContId))
  { supplier => power(supplier) }
  { unpower() }
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {

  watch(contIsClosedBy(GenTwoContId), contIsClosedBy(AcBusTwoTieContId))
  { supplier => power(supplier) }
  { unpower() }
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

class DcBatteryBus()(implicit ctx: SimulationContext) extends Bus(ctx, DcBatteryBusId) {

  watch(
    contIsClosedBy(DcTieOneContId),
    contIsClosedBy(DcTieTwoContId) when contIsOpen(DcTieOneContId),
    contIsClosedBy(BatteryOneContId) when (
      contIsOpen(DcTieOneContId) and contIsOpen(DcTieTwoContId)),
    contIsClosedBy(BatteryTwoContId) when (
      contIsOpen(DcTieOneContId) and contIsOpen(DcTieTwoContId))
  )
  { supplier => power(supplier) }
  { unpower() }
}

class HotBus(ctx: SimulationContext, busId: DeviceId, batteryId: DeviceId)
    extends Bus(ctx, busId) {

  watch(deviceIsInitialized(batteryId))
  {
    case true => power(batteryId)
    case false => unpower()
  }
}

class HotBusOne()(implicit ctx: SimulationContext) extends HotBus(ctx, HotBusOneId, BatteryOneId)

class HotBusTwo()(implicit ctx: SimulationContext) extends HotBus(ctx, HotBusTwoId, BatteryTwoId)

object Bus {

  sealed trait State
  case class Energized(by: DeviceId) extends State
  case object DeEnergized extends State
  val InitialState = DeEnergized

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasEnergized = StateChangedEvent(Some(DeEnergized), Energized)
  val WasUnenergized = StateChangedEvent(Some(Energized), DeEnergized)
}