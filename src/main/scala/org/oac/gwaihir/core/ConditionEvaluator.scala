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

/** An object that is conditioned by events occurred on some devices. */
trait EventConditioned {

  /** Retrieve the set of devices which events are conditioning this object. */
  def conditionedBy: Set[DeviceId]
}

/** A condition that may be evaluated by a condition watcher.
  *
  * Objects of these class are used to evaluate conditions that may derive in an action to
  * be triggered when the condition is met.
  *
  * Conditions are aimed to be watched by ConditionWatcher objects, which will retrieve the
  * devices whose events are affecting the condition and will subscribe to them. When any of
  * these devices send a event, the watcher is notified and it will evaluate the condition.
  * If the result of the evaluation is Some(), its contents are passed to the action associated
  * with the watcher. If the result is None, the condition is considered as not-met, and nothing
  * is done. 
  */
trait Condition[T] extends EventConditioned {

  /** The context that is passed to the conditions for its evaluation.
    *
    * It's made by a map of DeviceId to any object. Each entry represents the last event that
    * was sent for the given device.
    */
  type EvalContext = Map[DeviceId, Any]

  /** Evaluate the condition.
    *
    * This function is invoked by the watcher to determine whether the condition is met. A
    * result of Some(e) indicates that the condition is met, being e the result of the
    * evaluation. If the condition is not met, None is returned.
    */
  def eval(ctx: EvalContext): Option[T]

  /** Convenience function to create a WhenCondition. */
  def when(c: Condition[Boolean]): Condition[T] = WhenCondition(this, c)
}

/** A trait that provides some convenience functions to perform boolean logic. */
trait BooleanLogic {

  self: Condition[Boolean] =>

  /** Create a new logic-and condition from this one and the one passed as argument.  */
  def and(c: Condition[Boolean]) = new AndCondition(this, c)

  /** Create a new logic-or condition from this one and the one passed as argument.  */
  def or(c: Condition[Boolean]) = new OrCondition(this, c)

  /** Create a new logic-xor condition from this one and the one passed as argument.  */
  def xor(c: Condition[Boolean]) = new XorCondition(this, c)
}

/** A event-conditioned object that depends not in any event from any device. */
trait NoEventConditioned extends EventConditioned {

  override def conditionedBy = Set.empty
}

/** A event-conditioned object that depends on events of a single event. */
trait UnaryEventConditioned extends EventConditioned {

  def c: EventConditioned

  override def conditionedBy = c.conditionedBy
}

/** A event-conditioned object that depends on events of two devices. */
trait BinaryEventConditioned extends EventConditioned {

  def c1: EventConditioned
  def c2: EventConditioned

  override def conditionedBy = c1.conditionedBy ++ c2.conditionedBy
}

/** A condition that always evaluates to true. */
object TrueCondition extends Condition[Boolean] with BooleanLogic with NoEventConditioned {

  def eval(ctx: EvalContext) = Some(true)
}

/** A boolean condition that always evaluates to false. */
object FalseCondition extends Condition[Boolean] with BooleanLogic with NoEventConditioned {

  def eval(ctx: EvalContext) = Some(false)
}

/** A boolean condition that evaluates to the negation of another boolean condition. */
final case class NotCondition(c: Condition[Boolean])
    extends Condition[Boolean] with BooleanLogic with UnaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Boolean] = c.eval(ctx) match {
    case None => None
    case Some(x) => Some(!x)
  }
}

/** A boolean condition that evaluates to the logic-and of other two boolean conditions. */
final case class AndCondition(c1: Condition[Boolean], c2: Condition[Boolean])
    extends Condition[Boolean] with BooleanLogic with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(false)) => Some(false)
    case (Some(false), Some(_)) => Some(false)
    case _ => Some(true)
  }
}

/** A boolean condition that evaluates to the logic-or of other two boolean conditions. */
case class OrCondition(c1: Condition[Boolean], c2: Condition[Boolean])
    extends Condition[Boolean] with BooleanLogic with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(_), Some(true)) => Some(true)
    case (Some(true), Some(_)) => Some(true)
    case _ => Some(false)
  }
}

/** A boolean condition that evaluates to the logic-xor of other two boolean conditions. */
case class XorCondition(c1: Condition[Boolean], c2: Condition[Boolean])
    extends Condition[Boolean] with BooleanLogic with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Boolean] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, _) => None
    case (_, None) => None
    case (Some(false), Some(true)) => Some(true)
    case (Some(true), Some(false)) => Some(true)
    case _ => Some(false)
  }
}

/** A condition that evaluates another condition when a boolean condition is met. */
case class WhenCondition[T](c1: Condition[T], c2: Condition[Boolean])
    extends Condition[T] with BinaryEventConditioned {

  def eval(ctx: EvalContext): Option[T] = c2.eval(ctx) match {
    case Some(true) => c1.eval(ctx)
    case _ => None
  }
}

/** A condition that evaluates based on the last-known value of an event.
  *
  * @param dev The device which events are to be matched
  * @param pred The predicate that determines whether the condition is met
  */
case class EventMatchCondition[T](dev: DeviceId)(pred: PartialFunction[Any, Option[T]])
    extends Condition[T] with EventConditioned {

  override def conditionedBy = Set(dev)

  override def eval(ctx: EvalContext) = ctx.get(dev) match {
    case Some(event) => pred(event)
    case None => None
  }
}

/** An object that watchs conditions expecting them to be met.
  *
  * A condition watcher wraps a condition object with the purpose be aware when that condition
  * is met. Upon creation, it retrieves the set of devices whose events the condition depends on.
  * Then it subscribes to the event channel for any event on these devices. When one of these
  * events is received, the condition is evaluated. If the result of the evaluation is Some(),
  * then the condition is considered to met and the action associated to the watcher is
  * executed.
  *
  * @param eventChannel The event channel to watch for condition inputs.
  * @param cond The condition to watch
  * @param whenMet The action to be executed when condition is met
  */
class ConditionWatcher[T](eventChannel: EventChannel)
                         (watched: (Condition[T], T => Unit)*)
                         (whenNotMet: => Unit){

  var ctx: Map[DeviceId, Any] = Map.empty

  for {
    (cond, _) <- watched
    dev <- cond.conditionedBy
  } eventChannel.subscribe(dev)(onNewEvent)

  private def onNewEvent: EventChannel.Subscription = {
    case (sender, event) =>
      ctx += sender -> event
      var notMatched = true
      for ((cond, act) <- watched if notMatched) cond.eval(ctx) match {
        case Some(v) =>
          notMatched = false
          act(v)
        case _ => ()
      }
      if (notMatched) { whenNotMet }
    case _ => ()
  }
}

/** A trait for objects able to watch conditions. */
trait ConditionEvaluator {

  self: EventChannelProvider =>

  def not(cond: Condition[Boolean]): Condition[Boolean] = new NotCondition(cond)

  def watch[T](watched: (Condition[T], T => Unit)*)(whenNotMet: => Unit) {
    new ConditionWatcher(eventChannel)(watched: _*)(whenNotMet)
  }

  def watch[T](cond: Condition[T]*)(whenMet: T => Unit)(whenNotMet: => Unit) {
    watch(cond.map(c => (c, whenMet)): _*)(whenNotMet)
  }

  def watch(cond: Condition[Boolean])(whenMet: Boolean => Unit) {
    watch[Boolean](cond)(whenMet)(())
  }

  def eventMatch(dev: DeviceId, pred: PartialFunction[Any, Option[Boolean]]): BooleanCondition =
    new EventMatchCondition[Boolean](dev)(pred) with BooleanLogic {}

  def deviceIsInitialized(dev: DeviceId): BooleanCondition = eventMatch(dev, {
    case StateChangedEvent(None, _) => Some(true)
    case _ => None
  })

  def deviceStateChanged[State : Manifest, T](dev: DeviceId)(eval: State => Option[T]): Condition[T] =
    EventMatchCondition[T](dev) {
      case StateChangedEvent(_, state: State) => for { result <- eval(state) } yield result
      case _ => None
    }

  def deviceIs[State](dev: DeviceId, state: State): BooleanCondition = eventMatch(dev, {
    case StateChangedEvent(_, `state`) => Some(true)
    case _ => Some(false)
  })

  def deviceIs[State : Manifest](dev: DeviceId)
                                (pred: PartialFunction[State, Boolean]): BooleanCondition =
    eventMatch(dev, {
      case StateChangedEvent(_, s: State) => Some(pred(s))
      case _ => Some(false)
    })
}
