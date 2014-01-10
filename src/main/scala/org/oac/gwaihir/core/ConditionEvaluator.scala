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

trait Condition {
  def eval: Option[Boolean]
  def and(c: Condition) = new AndCondition(this, c)
  def or(c: Condition) = new OrCondition(this, c)
}

case object TrueCondition extends Condition { def eval = Some(true) }
case object FalseCondition extends Condition { def eval = Some(false) }

case class NotCondition(c: Condition) extends Condition {
  override def eval: Option[Boolean] = c.eval match {
    case None => None
    case Some(x) => Some(!x)
  }
}

case class AndCondition(c1: Condition, c2: Condition) extends Condition {
  override def eval: Option[Boolean] = (c1.eval, c2.eval) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(false)) => Some(false)
    case (Some(false), Some(_)) => Some(false)
    case _ => Some(true)
  }
}

case class OrCondition(c1: Condition, c2: Condition) extends Condition {
  override def eval: Option[Boolean] = (c1.eval, c2.eval) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(true)) => Some(true)
    case (Some(true), Some(_)) => Some(true)
    case _ => Some(false)
  }
}

case class EventMatchCondition(
    evaluator: ConditionEvaluator,
    channel: EventChannel,
    dev: DeviceId,
    pred: PartialFunction[Any, Boolean]) extends Condition {
  var evaluation: Option[Boolean] = None
  override def eval = evaluation

  val onEvent: PartialFunction[(DeviceId, Any), Unit] = {
    case (sender: DeviceId, event: Any) =>
      val prev = evaluation
      evaluation = Some(pred(event))
      if (prev != eval)
        evaluator.eval()
  }

  channel.subscribe(dev, onEvent)
}

trait ConditionEvaluator {

  self: EventChannelProvider =>

  val condition: Condition

  def whenConditionIsMet: Unit
  def whenConditionIsNotMet: Unit

  def eventMatch(dev: DeviceId, pred: PartialFunction[Any, Boolean]) =
    new EventMatchCondition(this, eventChannel, dev, pred)

  private [core] def eval() {
    condition.eval match {
      case Some(true) => whenConditionIsMet
      case Some(false) => whenConditionIsNotMet
      case _ => ()
    }
  }
}
