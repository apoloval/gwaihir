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

package gwaihir.a320.system.elec

import gwaihir.core._
import gwaihir.a320.common.Switch

/** A trait providing matching conditions for events sent by buses. */
trait BusConditions {
  self: ConditionEvaluator =>

  /** A condition consistent of the given bus to be in the given state. */
  def busIs(busId: DeviceId, state: Bus.State): Condition[DeviceId]  = deviceIs(busId, state)

  /** A condition consisting of the given bus to be energized. */
  def busIsEnergized(busId: DeviceId): Condition[DeviceId] = deviceIs[Bus.State, DeviceId](busId) {
    case Bus.Energized(_) => Some(busId)
    case _ => None
  }

  /** A condition consisting of the given bus to be energized by given supplier. */
  def busIsEnergizedBy(busId: DeviceId): Condition[(DeviceId, Seq[DeviceId])] =
    deviceStateChanged[Bus.State](busId).map {
      case (_, Bus.Energized(supplyChain)) => Some(busId -> supplyChain)
      case _ => None
    }

  /** A condition consisting of the given bus to be energized by given supplier. */
  def busIsEnergizedBy(busId: DeviceId, from: DeviceId): Condition[(DeviceId, Seq[DeviceId])] =
    deviceStateChanged[Bus.State](busId).map {
      case (_, Bus.Energized(supplyChain @ `from` :: _)) => Some(busId -> supplyChain)
      case _ => None
    }

  /** A condition consisting of the given bus to be unenergized. */
  def busIsUnenergized(busId: DeviceId): Condition[DeviceId] = busIs(busId, Bus.DeEnergized)
}

abstract class Bus(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Bus.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Bus._

  override def initialState = Bus.DeEnergized

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  def power(supplyChain: Seq[DeviceId]) { setState(Energized(supplyChain)) }
  def power(supply: DeviceId) { power(Seq(supply)) }
  def unpower() { setState(DeEnergized) }
}

class AcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusOneId) {

  watch(
    genIsOn(GenOneId),
    genIsOn(ExtPowerId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto),
    genIsOn(ApuGenId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto),
    genIsOn(GenTwoId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto))
  { bus => power(bus) }
  { unpower() }
}

class AcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, AcBusTwoId) {

  watch(
    genIsOn(GenTwoId),
    genIsOn(ExtPowerId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto),
    genIsOn(ApuGenId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto),
    genIsOn(GenOneId) when switchIs(AcBusTieSwitchId, AcBusTieSwitch.Auto))
  { bus => power(bus) }
  { unpower() }
}

class AcEssBus()(implicit ctx: SimulationContext) extends Bus(ctx, AcEssBusId) {

  watch(
    genIsOnBy(EmerGenId),
    busIsEnergizedBy(AcBusOneId) when switchIs(AcEssFeedSwitchId, AcEssFeedSwitch.Norm),
    busIsEnergizedBy(AcBusTwoId) when switchIs(AcEssFeedSwitchId, AcEssFeedSwitch.Alt)
  )
  { case (bus, supplyChain) => power(bus +: supplyChain) }
  { unpower() }
}

class DcBusOne()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusOneId) {

  watch(
    trIsPoweredBy(TrOneId),
    busIsEnergizedBy(DcBatteryBusId, DcBusTwoId)
  )
  {
    case (TrOneId, _) => power(TrOneId)
    case (DcBatteryBusId, supplyChain) => power(DcBatteryBusId +: supplyChain)
  }
  { unpower() }
}

class DcBusTwo()(implicit ctx: SimulationContext) extends Bus(ctx, DcBusTwoId) {

  watch(
    trIsPoweredBy(TrTwoId),
    busIsEnergizedBy(DcBatteryBusId, DcBusOneId)
  )
  {
    case (TrTwoId, _) => power(TrTwoId)
    case (DcBatteryBusId, supplyChain) => power(DcBatteryBusId +: supplyChain)
  }
  { unpower() }
}

class DcBatteryBus()(implicit ctx: SimulationContext) extends Bus(ctx, DcBatteryBusId) {

  watch(
    busIsEnergizedBy(DcBusOneId, TrOneId),
    busIsEnergizedBy(DcBusTwoId, TrTwoId)
  )
  { case (bus, supplyChain) => power(bus +: supplyChain) }
  { unpower() }
}

class HotBus(ctx: SimulationContext, busId: DeviceId, batteryId: DeviceId)
    extends Bus(ctx, busId) {

  watch(deviceIsInitialized(batteryId))
  { batt => power(Seq(batt)) }
  { unpower() }
}

class HotBusOne()(implicit ctx: SimulationContext) extends HotBus(ctx, HotBusOneId, BatteryOneId)

class HotBusTwo()(implicit ctx: SimulationContext) extends HotBus(ctx, HotBusTwoId, BatteryTwoId)

class DcEssentialBus()(implicit ctx: SimulationContext) extends Bus(ctx, DcEssBusId) {

  watch(
    busIsEnergizedBy(DcBatteryBusId) when (trIsPowered(TrOneId) and trIsPowered(TrTwoId)),
    trIsPoweredBy(EssTrId) when (trIsUnpowered(TrOneId) or trIsUnpowered(TrTwoId))
  )
  {
    case (DcBatteryBusId, supplyChain) => power(DcBatteryBusId +: supplyChain)
    case (EssTrId, supplyChain) => power(EssTrId)
  }
  { unpower() }
}

object Bus {

  sealed trait State

  case class Energized(supplyChain: Seq[DeviceId]) extends State

  object Energized {

    def apply(supply: DeviceId, moreSupply: DeviceId*): Energized =
      Energized(Seq(supply) ++ moreSupply)
  }

  case object DeEnergized extends State

  val InitialState = DeEnergized

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasEnergized = StateChangedEvent(Some(DeEnergized), Energized)
  val WasUnenergized = StateChangedEvent(Some(Energized), DeEnergized)
}