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
  def contIs(contId: DeviceId, state: Contactor.State) = deviceIs(contId, state)

  /** A condition consisting of the given contactor to be open. */
  def contIsOpen(contId: DeviceId) = contIs(contId, Contactor.Open)

  /** A condition consisting of the given contactor to be closed. */
  def contIsClosed(contId: DeviceId) = contIs(contId, Contactor.Closed)

  /** A condition consisting of any of the given contactors to be closed. */
  def anyContIsClosed(contId: DeviceId*) = contId.foldLeft[Condition](FalseCondition) {
    (cond, dev) => cond or contIsClosed(dev)
  }

  /** A condition consisting of any of the given contactors to be open. */
  def anyContIsOpen(contId: DeviceId*) = contId.foldLeft[Condition](FalseCondition) {
    (cond, dev) => cond or contIsOpen(dev)
  }

  /** A condition consisting of none of the given contactors to be closed. */
  def noneContIsClosed(contId: DeviceId*) = contId.foldLeft[Condition](TrueCondition) {
    (cond, dev) => cond and contIsOpen(dev)
  }

  /** A condition consisting of none of the given contactors to be closed. */
  def noneContIsOpen(contId: DeviceId*) = contId.foldLeft[Condition](TrueCondition) {
    (cond, dev) => cond and contIsClosed(dev)
  }
}

abstract class Contactor(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Contactor.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import Contactor._

  override def initialState = Contactor.Open

  def contactorIsClosed: Condition

  def open() = setState(Open)
  def close() = setState(Closed)

  watch(contactorIsClosed) { close() }
  watch(not(contactorIsClosed)) { open() }
}

class GenOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, GenOneContId) {

  override def contactorIsClosed = genIsOn(GenOneId)
}

class GenTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, GenTwoContId) {

  override def contactorIsClosed = genIsOn(GenTwoId)
}

class ApuGenContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ApuGenContId) {

  override def contactorIsClosed = genIsOn(ApuGenId) and contIsOpen(ExtPowerContId) and
    anyContIsOpen(GenOneContId, GenTwoContId)
}

class ExtPowerContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ExtPowerContId) {

  override def contactorIsClosed = genIsOn(ExtPowerId) and anyContIsOpen(GenOneContId, GenTwoContId)
}

class AcBusOneTieContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcBusOneTieContId) {

  private def flowLeftToRight =
    contIsClosed(GenOneContId) and noneContIsClosed(GenTwoContId, ApuGenContId, ExtPowerContId)
  private def flowRightToLeft =
    contIsOpen(GenOneContId) and anyContIsClosed(GenTwoContId, ApuGenContId, ExtPowerContId)

  override def contactorIsClosed = flowLeftToRight or flowRightToLeft
}

class AcBusTwoTieContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcBusTwoTieContId) {

  private def flowLeftToRight =
    contIsOpen(GenTwoContId) and anyContIsClosed(GenOneContId, ApuGenContId, ExtPowerContId)
  private def flowRightToLeft =
    contIsClosed(GenTwoContId) and noneContIsClosed(GenOneContId, ApuGenContId, ExtPowerContId)

  override def contactorIsClosed = flowLeftToRight or flowRightToLeft
}

class AcEssFeedNormContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedNormContactorId) {

  override def contactorIsClosed = busIsEnergized(AcBusOneId) and switchIsOff(AcEssFeedSwitchId)
}

class AcEssFeedAltContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedAltContactorId) {

  override def contactorIsClosed = busIsEnergized(AcBusTwoId) and switchIsOn(AcEssFeedSwitchId)
}

class TrOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, TrOneContactorId) {

  override def contactorIsClosed = trIsPowered(TrOneId)
}

class TrTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, TrTwoContactorId) {

  override def contactorIsClosed = trIsPowered(TrTwoId)
}

class DcTieOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcTieOneContId) {

  override def contactorIsClosed = busIsEnergized(DcBusOneId) or busIsEnergized(DcBusTwoId)
}

class DcTieTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, DcTieTwoContId) {

  override def contactorIsClosed = busIsEnergized(DcBusOneId) xor busIsEnergized(DcBusTwoId)
}

object Contactor {

  sealed trait State
  case object Open extends State
  case object Closed extends State
  val InitialState = Open

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasClosed = StateChangedEvent(Some(Open), Closed)
  val WasOpened = StateChangedEvent(Some(Closed), Open)
}
