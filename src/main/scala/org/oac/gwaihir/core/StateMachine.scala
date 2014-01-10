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

package org.oac.gwaihir.core

case class StateChangedEvent[State](from: Option[State], to: State)

/** An object that behaves as a state machine. */
trait StateMachine[State] {

  self: Device with SimulationContextAware =>

  private var _state: State = initialState

  /** The initial state of the machine. */
  val initialState: State

  override def init() = ctx.eventChannel.send(id, StateChangedEvent(None, initialState))

  /** Retreieve the current state of the machine. */
  def state = _state

  /** Set a new state for this machine
    *
    * If the new state is different than the current one, a StateChangedEvent is sent
    * to the event channel managed by the SimulationContext.
    */
  def setState(s: State) = (_state, s) match {
    case (s1, s2) if s1 != s2 =>
      _state = s2
      ctx.eventChannel.send(id, StateChangedEvent(Some(s1), s2))
    case _ =>
  }
}
