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

package org.oacsd.gwaihir.aircraft.a320.system.elec

import org.oacsd.gwaihir.core.{Device, SimulationContext, ConditionEvaluator, DeviceId}

import ElectricalSystem._

trait GeneratorConditions {

  self: ConditionEvaluator =>

  /** A condition consisting of the given generator to be on. */
  def genIsOn(genId: DeviceId) = eventMatch(genId, {
    case Generator.StateChangedEvent(_, Generator.PowerOn) => true
    case _ => false
  })

  /** A condition consisting of the given generator to be off. */
  def genIsOff(genId: DeviceId) = eventMatch(genId, {
    case Generator.StateChangedEvent(_, Generator.PowerOff) => true
    case _ => false
  })
}

class Generator(ctx: SimulationContext, val id: DeviceId) extends Device {

  import Generator._

  var _state: State = PowerOff

  override def init() = ctx.eventChannel.send(id, WasInitialized)

  def state = _state

  def powerOn() = _state match {
    case PowerOn =>
    case PowerOff =>
      _state = PowerOn
      ctx.eventChannel.send(id, WasPoweredOn)
  }

  def powerOff() = _state match {
    case PowerOn =>
      _state = PowerOff
      ctx.eventChannel.send(id, WasPoweredOff)
    case PowerOff =>
  }
}

class GenOne()(implicit ctx: SimulationContext) extends Generator(ctx, GenOneId)
class GenTwo()(implicit ctx: SimulationContext) extends Generator(ctx, GenTwoId)
class ApuGen()(implicit ctx: SimulationContext) extends Generator(ctx, ApuGenId)
class ExtPower()(implicit ctx: SimulationContext) extends Generator(ctx, ExtPowerId)

object Generator {

  trait State
  case object PowerOn extends State
  case object PowerOff extends State
  val InitialState = PowerOff

  case class StateChangedEvent(oldState: Option[State], newState: State)
  object WasInitialized extends StateChangedEvent(None, InitialState)
  object WasPoweredOn extends StateChangedEvent(Some(PowerOff), PowerOn)
  object WasPoweredOff extends StateChangedEvent(Some(PowerOn), PowerOff)
}
