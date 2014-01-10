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
  * Objects of these class are used to evaluate conditions that may derive in two actions to
  * be triggered: one when the condition is met and other when the condition is not met.
  * Conditions are watched by ConditionWatcher objects. If the condition changes, the watcher
  * is requested to watch it by evaluating and executing the corresponding action.
  */
abstract class Condition {

  /** Evaluate the condition.
    *
    * This function is invoked by the watcher to determine whether the condition is met
    * (Some(true)) or not (Some(false)). It may return None if the condition cannot be determined
    * by some reason (i.e. no enough information is still available).
    */
  def eval: Option[Boolean]

  /** Set the watcher for this condition.
    *
    * Conditions are responsible of notifying the watcher when they change. This function may be
    * used to set the watcher that will be notified when the condition changes.
    */
  def setWatcher(w: ConditionWatcher)

  /** Create a new logic-and condition from this one and the one passed as argument.  */
  def and(c: Condition) = new AndCondition(this, c)

  /** Create a new logic-or condition from this one and the one passed as argument.  */
  def or(c: Condition) = new OrCondition(this, c)
}

/** A condition that can be evaluated without comprising any other condition. */
abstract class SingleCondition extends Condition {

  var watcher: Option[ConditionWatcher] = None

  override def setWatcher(w: ConditionWatcher) { watcher = Some(w) }

  protected def informWatcher() = if (watcher.isDefined) { watcher.get.watch() }
}

/** A condition that is evaluated by some other conditions. */
abstract class CompositeCondition(c: Condition*) extends Condition {

  override def setWatcher(w: ConditionWatcher) = c.foreach(_.setWatcher(w))
}

/** A condition that always evaluates to true. */
final class TrueCondition extends SingleCondition { def eval = Some(true) }

/** A condition that always evaluates to false. */
final class FalseCondition extends SingleCondition { def eval = Some(false) }

/** A condition that evaluates to the negation of the result of another condition. */
final class NotCondition(c: Condition) extends CompositeCondition(c) {

  override def eval: Option[Boolean] = c.eval match {
    case None => None
    case Some(x) => Some(!x)
  }
}

/** A condition that evaluates to the logic-and of the result of two other conditions. */
case class AndCondition(c1: Condition, c2: Condition) extends CompositeCondition(c1, c2) {

  override def eval: Option[Boolean] = (c1.eval, c2.eval) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(false)) => Some(false)
    case (Some(false), Some(_)) => Some(false)
    case _ => Some(true)
  }
}

/** A condition that evaluates to the logic-or of the result of two other conditions. */
case class OrCondition(c1: Condition, c2: Condition) extends CompositeCondition(c1, c2) {

  override def eval: Option[Boolean] = (c1.eval, c2.eval) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(true)) => Some(true)
    case (Some(true), Some(_)) => Some(true)
    case _ => Some(false)
  }
}

/** A condition that evaluates based on the last value of a event.
  *
  * This condition object listens on a channel for events on a determined device. It uses a
  * predicate to determine whether the event met or not the condition. When a change is
  * detected in the condition, the watcher is requested to watch the condition (what will
  * cause the met/not met action to be executed).
  *
  * @param channel The event channel to listen for events
  * @param dev The device which events are to be matched
  * @param pred The predicate that determines whether the condition is met or not
  */
case class EventMatchCondition(
    channel: EventChannel,
    dev: DeviceId,
    pred: PartialFunction[Any, Boolean]) extends SingleCondition {

  private var evaluation: Option[Boolean] = None
  override def eval = evaluation

  channel.subscribe(dev) {
    case (sender: DeviceId, event: Any) =>
      val prev = evaluation
      evaluation = Some(pred(event))
      if (prev != eval) { informWatcher() }
  }
}

/** An object that watchs conditions expecting them to be met (or not).
  *
  * @param cond The condition to watch
  * @param whenMet The action to be executed when condition is met
  * @param whenNotMet The action to be executed when condition is not met
  */
class ConditionWatcher(cond: Condition, whenMet: => Unit, whenNotMet: => Unit) {

  def watch() = cond.eval match {
    case Some(true) => whenMet
    case Some(false) => whenNotMet
    case _ => ()
  }

  cond.setWatcher(this)
}

/** A trait for objects able to watch conditions. */
trait ConditionEvaluator {

  self: EventChannelProvider =>

  private var watchers: Set[ConditionWatcher] = Set.empty

  def watch(cond: Condition)(whenMet: => Unit)(whenNotMet: => Unit) =
    watchers += new ConditionWatcher(cond, whenMet, whenNotMet)

  def eventMatch(dev: DeviceId, pred: PartialFunction[Any, Boolean]) =
    new EventMatchCondition(eventChannel, dev, pred)

  def deviceIs[State](dev: DeviceId, state: State) = eventMatch(dev, {
    case StateChangedEvent(_, `state`) => true
    case _ => false
  })
}
