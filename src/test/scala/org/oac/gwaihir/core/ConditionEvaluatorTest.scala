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

import org.scalatest.{Matchers, FlatSpec}

case class DummyEvaluator(eventChannel: EventChannel)
    extends ConditionEvaluator with EventChannelProvider {

  val dev1 = DeviceId("foobar/dev1")
  val dev2 = DeviceId("foobar/dev2")

  var matches: Option[Boolean] = None

  val dev1IsOn = eventMatch(dev1, {
    case (isOn: Boolean) => Some(dev1 -> isOn)
    case _ => None
  })
  val dev2IsOver100 = eventMatch(dev2, {
    case (power: Int) if power > 100 => Some(dev2 -> power)
    case _ => None
  })

  watch(dev1IsOn and dev2IsOver100)
  { _ => matches = Some(true) }
  { matches = Some(false) }
}

class ConditionEvaluatorTest extends FlatSpec with Matchers {

  "Condition evaluator" must "consider undetermined matching when no event is sent" in
    new EvaluatorInitialized {
      eval.matches should be (None)
    }

  it must "consider not matching when conditions are not met" in
    new EvaluatorInitialized {
      channel.send(eval.dev1, true)
      channel.send(eval.dev2, 10)
      eval.matches should be (Some(false))
    }

  it must "consider matching when all conditions are met" in
    new EvaluatorInitialized {
      channel.send(eval.dev1, true)
      channel.send(eval.dev2, 110)
      eval.matches should be (Some(true))
    }

  it must "consider not matching when any conditions is not met anymore" in
    new EvaluatorInitialized {
      channel.send(eval.dev1, true)
      channel.send(eval.dev2, 110)
      eval.matches should be (Some(true))
      channel.send(eval.dev2, 10)
      eval.matches should be (Some(false))
    }

  trait EvaluatorInitialized {
    val channel = EventChannel()
    val eval = DummyEvaluator(channel)
  }
}
