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

/** A condition that may be evaluated by a condition watcher.
  *
  * Objects of these class are used to evaluate conditions that may derive in an action to
  * be triggered when the condition is met. Conditions are watched by ConditionWatcher objects.
  * If the condition changes, the watchers are requested to watch it by evaluating and executing
  * the corresponding action if the condition matches.
  */
abstract class Condition {

  type Context = Map[DeviceId, Any]

  def conditionedBy: Seq[DeviceId]

  /** Evaluate the condition.
    *
    * This function is invoked by the watcher to determine whether the condition is met
    * (Some(true)) or not (Some(false)). It may return None if the condition cannot be determined
    * by some reason (i.e. no enough information is still available).
    */
  def eval(ctx: Context): Option[Boolean]

  /** Create a new logic-and condition from this one and the one passed as argument.  */
  def and(c: Condition) = new AndCondition(this, c)

  /** Create a new logic-or condition from this one and the one passed as argument.  */
  def or(c: Condition) = new OrCondition(this, c)

  /** Create a new logic-xor condition from this one and the one passed as argument.  */
  def xor(c: Condition) = new XorCondition(this, c)
}

/** A condition that depends not in any other condition. */
abstract class NullaryCondition extends Condition {

  override def conditionedBy = Seq.empty
}

/** A condition that depends on another condition. */
abstract class UnaryCondition extends Condition {

  def c: Condition

  override def conditionedBy = c.conditionedBy
}

/** A condition that depends in other two conditions. */
abstract class BinaryCondition extends Condition {

  def c1: Condition
  def c2: Condition

  override def conditionedBy = c1.conditionedBy ++ c2.conditionedBy
}

/** A condition that always evaluates to true. */
final object TrueCondition extends NullaryCondition {

  def eval(ctx: Context) = Some(true)
}

/** A condition that always evaluates to false. */
final object FalseCondition extends NullaryCondition {

  def eval(ctx: Context) = Some(false)
}

/** A condition that evaluates to the negation of the result of another condition. */
final case class NotCondition(c: Condition) extends UnaryCondition {

  override def eval(ctx: Context): Option[Boolean] = c.eval(ctx) match {
    case None => None
    case Some(x) => Some(!x)
  }
}

/** A condition that evaluates to the logic-and of the result of two other conditions. */
final case class AndCondition(c1: Condition, c2: Condition) extends BinaryCondition {

  override def eval(ctx: Context): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(false)) => Some(false)
    case (Some(false), Some(_)) => Some(false)
    case _ => Some(true)
  }
}

/** A condition that evaluates to the logic-or of the result of two other conditions. */
case class OrCondition(c1: Condition, c2: Condition) extends BinaryCondition {

  override def eval(ctx: Context): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(true)) => Some(true)
    case (Some(true), Some(_)) => Some(true)
    case _ => Some(false)
  }
}

/** A condition that evaluates to the logic-xor of the result of two other conditions. */
case class XorCondition(c1: Condition, c2: Condition) extends BinaryCondition {

  override def eval(ctx: Context): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(false), Some(true)) => Some(true)
    case (Some(true), Some(false)) => Some(true)
    case _ => Some(false)
  }
}

/** A condition that evaluates based on the last value of a event.
  *
  * This condition object listens on a channel for events on a determined device. It uses a
  * predicate to determine whether the event met or not the condition. When the event evaluation
  * changes respect the last evaluated value, the watcher is requested to watch the condition.
  *
  * @param dev The device which events are to be matched
  * @param pred The predicate that determines whether the condition is met
  */
case class EventMatchCondition(dev: DeviceId)
                              (pred: PartialFunction[Any, Boolean]) extends Condition {

  override def conditionedBy = Seq(dev)

  override def eval(ctx: Context) = ctx.get(dev) match {
    case Some(event) => Some(pred(event))
    case None => None
  }
}

/** An object that watchs conditions expecting them to be met.
  *
  * @param cond The condition to watch
  * @param whenMet The action to be executed when condition is met
  */
class ConditionWatcher(eventChannel: EventChannel, cond: Condition, whenMet: => Unit) {

  var ctx: Map[DeviceId, Any] = Map.empty

  cond.conditionedBy.foreach { dev =>
    eventChannel.subscribe(dev)(onNewEvent)
  }

  private def onNewEvent: EventChannel.Subscription = {
    case (sender, event) =>
      ctx += sender -> event
      if (cond.eval(ctx).getOrElse(false)) { whenMet }
      ()
    case _ => ()
  }
}

/** A trait for objects able to watch conditions. */
trait ConditionEvaluator {

  self: EventChannelProvider =>

  private var watchers: Set[ConditionWatcher] = Set.empty

  def not(cond: Condition): Condition = new NotCondition(cond)

  def watch(cond: Condition)(whenMet: => Unit) =
    watchers += new ConditionWatcher(eventChannel, cond, whenMet)

  def eventMatch(dev: DeviceId, pred: PartialFunction[Any, Boolean]): Condition =
    new EventMatchCondition(dev)(pred)

  def deviceIs[State](dev: DeviceId, state: State): Condition = eventMatch(dev, {
    case StateChangedEvent(_, `state`) => true
    case _ => false
  })

  def deviceIs[State : Manifest](dev: DeviceId)(pred: PartialFunction[State, Boolean]): Condition =
    eventMatch(dev, {
      case StateChangedEvent(_, s: State) => pred(s)
      case _ => false
    })
}
