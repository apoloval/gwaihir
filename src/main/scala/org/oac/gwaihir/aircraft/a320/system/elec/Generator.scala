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

import org.oacsd.gwaihir.core._

import ElectricalSystem._
import scala.Some
import org.oac.gwaihir.core.{StateChangedEvent, StateMachine}

trait GeneratorConditions {

  self: ConditionEvaluator =>

  /** A condition consisting of the given generator to be on given state. */
  def genIs(genId: DeviceId, state: Generator.State) = eventMatch(genId, {
    case StateChangedEvent(_, `state`) => true
    case _ => false
  })

  /** A condition consisting of the given generator to be on. */
  def genIsOn(genId: DeviceId) = genIs(genId, Generator.PowerOn)

  /** A condition consisting of the given generator to be off. */
  def genIsOff(genId: DeviceId) = genIs(genId, Generator.PowerOff)
}

class Generator(val ctx: SimulationContext, val id: DeviceId) extends Device
    with SimulationContextAware with StateMachine[Generator.State] {

  import Generator._

  override val initialState = PowerOff

  def powerOn() = setState(PowerOn)
  def powerOff() = setState(PowerOff)
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

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasPoweredOn = StateChangedEvent(Some(PowerOff), PowerOn)
  val WasPoweredOff = StateChangedEvent(Some(PowerOn), PowerOff)
}
