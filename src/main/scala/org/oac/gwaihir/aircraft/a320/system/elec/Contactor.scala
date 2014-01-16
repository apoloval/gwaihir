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

/** A trait providing matching conditions for events sent by contactors. */
trait ContactorConditions {

  self : ConditionEvaluator =>

  /** A condition consisting of the given contactor to be in the given state. */
  def contIs(contId: DeviceId, state: Contactor.State): BooleanCondition  = deviceIs(contId, state)

  /** A condition consisting of the given contactor to be open. */
  def contIsOpen(contId: DeviceId): BooleanCondition  = contIs(contId, Contactor.Open)

  /** A condition consisting of the given contactor to be closed. */
  def contIsClosed(contId: DeviceId): BooleanCondition  = deviceIs[Contactor.State](contId) {
    case Contactor.Closed(_) => true
    case _ => false
  }

  /** A condition consisting of the given contactor to be closed with some supplier. */
  def contIsClosedBy(contId: DeviceId): Condition[Seq[DeviceId]] =
    deviceStateChanged[Contactor.State, Seq[DeviceId]](contId) {
      case Contactor.Closed(supplierChain) => Some(contId +: supplierChain)
      case _ => None
    }

  /** A condition consisting of the given contactor to be closed with some supplier. */
  def contIsClosedBy(contId: DeviceId, from: DeviceId): Condition[Seq[DeviceId]] =
    deviceStateChanged[Contactor.State, Seq[DeviceId]](contId) {
      // case Contactor.Closed(supplierChain @ `from` :: _) => Some(contId +: supplierChain)
      case Contactor.Closed(supplierChain) if supplierChain.head == from => Some(contId +: supplierChain)
      case _ => None
    }

  /** A condition consisting of any of the given contactors to be closed. */
  def anyContIsClosed(contId: DeviceId*): BooleanCondition  =
    contId.foldLeft[BooleanCondition](FalseCondition) {
      (cond, dev) => cond or contIsClosed(dev)
    }

  /** A condition consisting of any of the given contactors to be open. */
  def anyContIsOpen(contId: DeviceId*): BooleanCondition  =
    contId.foldLeft[BooleanCondition](FalseCondition) {
      (cond, dev) => cond or contIsOpen(dev)
    }

  /** A condition consisting of none of the given contactors to be closed. */
  def noneContIsClosed(contId: DeviceId*): BooleanCondition =
    contId.foldLeft[BooleanCondition](TrueCondition) {
      (cond, dev) => cond and contIsOpen(dev)
    }

  /** A condition consisting of none of the given contactors to be closed. */
  def noneContIsOpen(contId: DeviceId*): BooleanCondition  =
    contId.foldLeft[BooleanCondition](TrueCondition) {
      (cond, dev) => cond and contIsClosed(dev)
    }
}

abstract class Contactor(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Contactor.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Contactor._

  override def initialState = Contactor.Open

  def open() { setState(Open) }

  def close(supplyChain: Seq[DeviceId]) { setState(Closed(supplyChain)) }

  def close(supply: DeviceId) { close(Seq(supply)) }
}

class GenContactor(ctx: SimulationContext, contId: DeviceId, genId: DeviceId)
    extends Contactor(ctx, contId) {

  watch(genIsOn(genId)) {
    case true => close(Seq(genId))
    case false => open()
  }
}

class GenOneContactor()(implicit ctx: SimulationContext)
  extends GenContactor(ctx, GenOneContId, GenOneId)

class GenTwoContactor()(implicit ctx: SimulationContext)
  extends GenContactor(ctx, GenTwoContId, GenTwoId)

class ApuGenContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ApuGenContId) {

  private val isClosed = genIsOn(ApuGenId) and
    contIsOpen(ExtPowerContId) and
    anyContIsOpen(GenOneContId, GenTwoContId)

  watch(isClosed) {
    case true => close(Seq(ApuGenId))
    case false => open()
  }
}

class ExtPowerContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ExtPowerContId) {

  watch(genIsOn(ExtPowerId) and anyContIsOpen(GenOneContId, GenTwoContId)) {
    case true => close(Seq(ExtPowerId))
    case false => open()
  }
}

class AcBusOneTieContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcBusOneTieContId) {

  val isClosedByGen1 = contIsClosed(GenOneContId) and
    noneContIsClosed(GenTwoContId, ApuGenContId, ExtPowerContId)
  val isClosedByApuGen = contIsOpen(GenOneContId) and contIsClosed(ApuGenContId)
  val isClosedByExtPower = contIsOpen(GenOneContId) and contIsClosed(ExtPowerContId)
  val isClosedByGen2 = contIsOpen(GenOneContId) and contIsClosed(GenTwoContId)
  val isOpen = not(isClosedByGen1 or isClosedByApuGen or isClosedByExtPower or isClosedByGen2)

  watch(isClosedByGen1) { isClosed => if (isClosed) { close(Seq(GenOneContId, GenOneId)) } }
  watch(isClosedByApuGen) { isClosed => if (isClosed) { close(Seq(ApuGenContId, ApuGenId)) } }
  watch(isClosedByExtPower) { isClosed => if (isClosed) { close(Seq(ExtPowerContId, ExtPowerId)) } }
  watch(isClosedByGen2) { isClosed => if (isClosed) { close(Seq(GenTwoContId, GenTwoId)) } }
  watch(isOpen) { isOpen => if (isOpen) { open() } }
}

class AcBusTwoTieContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcBusTwoTieContId) {

  val isClosedByGen2 = contIsClosed(GenTwoContId) and
    noneContIsClosed(GenOneContId, ApuGenContId, ExtPowerContId)
  val isClosedByApuGen = contIsOpen(GenTwoContId) and contIsClosed(ApuGenContId)
  val isClosedByExtPower = contIsOpen(GenTwoContId) and contIsClosed(ExtPowerContId)
  val isClosedByGen1 = contIsOpen(GenTwoContId) and contIsClosed(GenOneContId)
  val isOpen = not(isClosedByGen2 or isClosedByApuGen or isClosedByExtPower or isClosedByGen1)

  watch(isClosedByGen2) { isClosed => if (isClosed) { close(Seq(GenTwoContId, GenTwoId)) } }
  watch(isClosedByApuGen) { isClosed => if (isClosed) { close(Seq(ApuGenContId, ApuGenId)) } }
  watch(isClosedByExtPower) { isClosed => if (isClosed) { close(Seq(ExtPowerContId, ExtPowerId)) } }
  watch(isClosedByGen1) { isClosed => if (isClosed) { close(Seq(GenOneContId, GenOneId)) } }
  watch(isOpen) { isOpen => if (isOpen) { open() } }
}

class AcEssFeedNormContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedNormContactorId) {

  watch(busIsEnergizedBy(AcBusOneId) when switchIsOff(AcEssFeedSwitchId))
  { poweredBy => close(poweredBy) }
  { open() }
}

class AcEssFeedAltContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedAltContactorId) {

  watch(busIsEnergizedBy(AcBusTwoId) when switchIsOn(AcEssFeedSwitchId))
  { poweredBy => close(poweredBy) }
  { open() }
}

class TrOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, TrOneContactorId) {

  watch(trIsPowered(TrOneId)) {
    case true => close(Seq(TrOneId))
    case false => open()
  }
}

class TrTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, TrTwoContactorId) {

  watch(trIsPowered(TrTwoId)) {
    case true => close(Seq(TrTwoId))
    case false => open()
  }
}

class DcTieOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcTieOneContId) {

  watch(
    busIsEnergizedBy(DcBusOneId, TrOneContactorId),
    busIsEnergizedBy(DcBatteryBusId, DcTieTwoContId)
  )
  { supplyChain =>  close(supplyChain) }
  { open() }
}

class DcTieTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcTieTwoContId) {

  watch(
    busIsEnergizedBy(DcBusTwoId, TrTwoContactorId) when contIsOpen(TrOneContactorId),
    busIsEnergizedBy(DcBatteryBusId, DcTieOneContId)
  )
  { supplyChain =>  close(supplyChain) }
  { open() }
}

class DcEssTieContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcEssTieContId) {

  // TODO: watch
}

class EssTrContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcEssTrContId) {

  // TODO: watch
}

class StaticInvOneContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, StaticInvOneContId) {

  // TODO: watch
}

class StaticInvTwoContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, StaticInvTwoContId) {

  // TODO: watch
}

object Contactor {

  sealed trait State

  case object Open extends State

  case class Closed(supplyChain: Seq[DeviceId]) extends State

  object Closed {

    def apply(supply: DeviceId, moreSupply: DeviceId*): Closed = Closed(Seq(supply) ++ moreSupply)
  }

  val InitialState = Open

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasClosed = StateChangedEvent(Some(Open), Closed)
  val WasOpened = StateChangedEvent(Some(Closed), Open)
}
