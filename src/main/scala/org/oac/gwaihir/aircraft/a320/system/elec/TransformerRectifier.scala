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

trait TransformerRectifierConditions {

  self: ConditionEvaluator =>

  /** A condition consisting of the given TR to be in the given state. */
  def trIs(trId: DeviceId, state: TransformerRectifier.State): BooleanCondition =
    deviceIs(trId, state)

  /** A condition consisting of the given TR to be powered. */
  def trIsPowered(trId: DeviceId): BooleanCondition = deviceIs[TransformerRectifier.State](trId) {
    case TransformerRectifier.Powered(_) => true
    case _ => false
  }

  /** A condition consisting of the given TR to be powered by a supply. */
  def trIsPoweredBy(trId: DeviceId): Condition[Seq[DeviceId]] =
    deviceStateChanged[TransformerRectifier.State, Seq[DeviceId]](trId) {
      case TransformerRectifier.Powered(supply) => Some(trId +: supply)
      case _ => None
    }

  /** A condition consisting of the given TR to be unpowered. */
  def trIsUnpowered(trId: DeviceId): BooleanCondition =
    deviceIs(trId, TransformerRectifier.Unpowered)
}

abstract class TransformerRectifier(val ctx: SimulationContext, val id: DeviceId) extends Device
    with SimulationContextAware with StateMachine[TransformerRectifier.State]
    with ConditionEvaluator with ElectricalSystemConditions {

  import TransformerRectifier._

  override def initialState = Unpowered

  def power(supplyChain: Seq[DeviceId]) { setState(Powered(supplyChain)) }
  def power(supply: DeviceId) { power(Seq(supply)) }
  def unpower() { setState(Unpowered) }
}

class TrOne(implicit ctx: SimulationContext) extends TransformerRectifier(ctx, TrOneId) {

  watch(busIsEnergizedBy(AcBusOneId))
  { supply => power(supply)}
  { unpower() }
}

class TrTwo(implicit ctx: SimulationContext) extends TransformerRectifier(ctx, TrTwoId) {

  watch(busIsEnergizedBy(AcBusTwoId))
  { supply => power(supply)}
  { unpower() }
}

object TransformerRectifier {

  sealed trait State

  case class Powered(supplyChain: Seq[DeviceId]) extends State

  object Powered {

    def apply(supply: DeviceId, moreSupply: DeviceId*): Powered = Powered(Seq(supply) ++ moreSupply)
  }

  case object Unpowered extends State

  val InitialState = Unpowered

  val WasInitialized = StateChangedEvent(None, InitialState)
  val WasPowered = StateChangedEvent(Some(Unpowered), Powered)
  val WasUnpowered = StateChangedEvent(Some(Powered), Unpowered)
}
