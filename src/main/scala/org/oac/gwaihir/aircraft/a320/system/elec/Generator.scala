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

trait GeneratorConditions {

  self: ConditionEvaluator =>

  /** A condition consisting of the given generator to be on. */
  def genIsOn(genId: DeviceId): Condition[DeviceId] = deviceIs(genId, Generator.PowerOn)

  /** A condition consisting of the given generator to be on.
    *
    * This is a convenience function to adapt genIsOn() to produce a
    * Condition[(DeviceId, Seq[DeviceId])], with the gen as first element of the tuple and
    * Seq.empty and second element.
    */
  def genIsOnBy(genId: DeviceId): Condition[(DeviceId, Seq[DeviceId])] =
    genIsOn(genId).map { gen => Some(gen, Seq.empty) }

  /** A condition consisting of the given generator to be off. */
  def genIsOff(genId: DeviceId): Condition[DeviceId]  = deviceIs(genId, Generator.PowerOff)
}

class Generator(val ctx: SimulationContext, val id: DeviceId)
    extends Device with StateMachine[Generator.State] {

  import Generator._

  override def initialState = PowerOff

  def powerOn() = setState(PowerOn)
  def powerOff() = setState(PowerOff)
}

class GenOne()(implicit ctx: SimulationContext) extends Generator(ctx, GenOneId)
class GenTwo()(implicit ctx: SimulationContext) extends Generator(ctx, GenTwoId)
class ApuGen()(implicit ctx: SimulationContext) extends Generator(ctx, ApuGenId)
class ExtPower()(implicit ctx: SimulationContext) extends Generator(ctx, ExtPowerId)
class EmerGen()(implicit ctx: SimulationContext) extends Generator(ctx, EmerGenId)

object Generator {

  trait State
  case object PowerOn extends State
  case object PowerOff extends State
  val InitialState = PowerOff

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasPoweredOn = StateChangedEvent(Some(PowerOff), PowerOn)
  val WasPoweredOff = StateChangedEvent(Some(PowerOn), PowerOff)
}
