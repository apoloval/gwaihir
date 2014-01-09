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

/** A trait providing matching conditions for events sent by contactors. */
trait ContactorConditions {

  self : ConditionEvaluator =>

  /** A condition consisting of the given contactor to be in the given state. */
  def contIs(contId: DeviceId, state: Contactor.State) = eventMatch(contId, {
    case Contactor.StateChangedEvent(_, `state`) => true
    case _ => false
  })

  /** A condition consisting of the given contactor to be open. */
  def contIsOpen(contId: DeviceId) = contIs(contId, Contactor.Open)

  /** A condition consisting of the given contactor to be closed. */
  def contIsClosed(contId: DeviceId) = contIs(contId, Contactor.Closed)

  /** A condition consisting of any of the given contactors to be closed. */
  def anyContIsClosed(contId: DeviceId*) = contId.foldLeft[Condition](FalseCondition) {
    (cond, dev) => cond or contIsClosed(dev)
  }
}

abstract class Contactor(ctx: SimulationContext, val id: DeviceId)
    extends Device with ConditionEvaluator with ElectricalSystemConditions {

  import Contactor._

  override val channel: EventChannel = ctx.eventChannel

  override def whenConditionIsMet = close()
  override def whenConditionIsNotMet = open()

  var _state: State = Open

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  def state = _state

  def open() = _state match {
    case Open =>
    case Closed =>
      _state = Open
      ctx.eventChannel.send(id, WasOpened)
  }

  def close() = _state match {
    case Open =>
      _state = Closed
      ctx.eventChannel.send(id, WasClosed)
    case Closed =>
  }
}

class GenOneContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, GenOneContId) {
  override val condition = genIsOn(GenOneId)
}

class GenTwoContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, GenTwoContId) {
  override val condition = genIsOn(GenTwoId)
}

class ApuGenContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ApuGenContId) {
  override val condition =
    genIsOn(ApuGenId) and
      contIsOpen(GenOneContId) and
      contIsOpen(GenTwoContId) and
      contIsOpen(ExtPowerContId)
}

class ExtPowerContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, ExtPowerContId) {
  override val condition =
    genIsOn(ExtPowerId) and contIsOpen(GenOneContId) and contIsOpen(GenTwoContId)
}

class BusTieContactor()(implicit ctx: SimulationContext) extends Contactor(ctx, BusTieContId) {
  override val condition =
    anyContIsClosed(GenOneContId, GenTwoContId, ApuGenContId, ExtPowerContId) and
    (contIsOpen(GenOneContId) or contIsOpen(GenTwoContId))
}

class AcEssFeedNormContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedNormContactorId) {
  override val condition = busIsEnergized(AcBusOneId) and switchIsOff(AcEssFeedSwitchId)
}

class AcEssFeedAltContactor()(implicit ctx: SimulationContext)
    extends Contactor(ctx, AcEssFeedAltContactorId) {
  override val condition = busIsEnergized(AcBusTwoId) and switchIsOn(AcEssFeedSwitchId)
}

object Contactor {

  sealed trait State
  case object Open extends State
  case object Closed extends State
  val InitialState = Open

  case class StateChangedEvent(oldState: Option[State], newState: State)
  object WasInitialized extends StateChangedEvent(None, InitialState)
  object WasClosed extends StateChangedEvent(Some(Open), Closed)
  object WasOpened extends StateChangedEvent(Some(Closed), Open)
}
