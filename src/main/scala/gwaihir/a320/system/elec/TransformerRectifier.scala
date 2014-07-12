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

trait TransformerRectifierConditions {

  self: ConditionEvaluator =>

  /** A condition consisting of the given TR to be in the given state. */
  def trIs(trId: DeviceId, state: TransformerRectifier.State): Condition[DeviceId] =
    deviceIs(trId, state)

  /** A condition consisting of the given TR to be powered. */
  def trIsPowered(trId: DeviceId): Condition[DeviceId] =
    deviceIs[TransformerRectifier.State, DeviceId](trId) {
      case TransformerRectifier.Powered(_) => Some(trId)
      case _ => None
    }

  /** A condition consisting of the given TR to be powered by a supply. */
  def trIsPoweredBy(trId: DeviceId): Condition[(DeviceId, Seq[DeviceId])] =
    deviceStateChanged[TransformerRectifier.State](trId).map {
      case (_, TransformerRectifier.Powered(supply)) => Some(trId -> supply)
      case _ => None
    }

  /** A condition consisting of the given TR to be unpowered. */
  def trIsUnpowered(trId: DeviceId): Condition[DeviceId] =
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
  { case (bus, supplyChain) => power(bus +: supplyChain) }
  { unpower() }
}

class TrTwo(implicit ctx: SimulationContext) extends TransformerRectifier(ctx, TrTwoId) {

  watch(busIsEnergizedBy(AcBusTwoId))
  { case (bus, supplyChain) => power(bus +: supplyChain) }
  { unpower() }
}

class EssTr(implicit ctx: SimulationContext) extends TransformerRectifier(ctx, EssTrId) {

  watch(
    busIsEnergizedBy(AcEssBusId) when (
      (trIsUnpowered(TrOneId) or trIsUnpowered(TrTwoId)) and (genIsOff(EmerGenId))),
    genIsOnBy(EmerGenId)
  )
  { case (source, supplyChain) => power(source +: supplyChain) }
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
