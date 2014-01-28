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

  /** Create a new logic-and condition from this one and the one passed as argument.  */
  def and[T1](c: Condition[T1]) = new AndCondition(this, c)

  /** Create a new logic-or condition from this one and the one passed as argument.  */
  def or[T1](c: Condition[T1]) = new OrCondition(this, c)

  /** Create a new logic-xor condition from this one and the one passed as argument.  */
  def xor[T1](c: Condition[T1]) = new XorCondition(this, c)

  /** Convenience function to create a WhenCondition. */
  def when[T1](c: Condition[T1]): Condition[T] = WhenCondition(this, c)

  /** Map this condition to another evaluation type. */
  def map[T1](f: T => Option[T1]) = new MappedCondition[T, T1](this, f)
}

/** A condition that is mapping another condition.
  *
  * @param mapped The condition that is being mapped
  * @param mapFunc The function used to map the condition
  * @tparam T1 The evaluation type of the mapped condition
  * @tparam T2 The evaluation type of the mapper condition
  */
class MappedCondition[T1, T2](mapped: Condition[T1], mapFunc: T1 => Option[T2]) extends Condition[T2] {

  def eval(ctx: EvalContext): Option[T2] = mapped.eval(ctx) match {
    case Some(expr) => mapFunc(expr)
    case None => None
  }

  def conditionedBy = mapped.conditionedBy
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
object TrueCondition extends Condition[Unit] with NoEventConditioned {

  def eval(ctx: EvalContext) = Some(())
}

/** A boolean condition that always evaluates to false. */
object FalseCondition extends Condition[Unit] with NoEventConditioned {

  def eval(ctx: EvalContext) = None
}

/** A boolean condition that evaluates to the negation of another boolean condition. */
final case class NotCondition[T](c: Condition[T])
    extends Condition[Unit] with UnaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Unit] = c.eval(ctx) match {
    case None => Some(())
    case Some(_) => None
  }
}

/** A boolean condition that evaluates to the logic-and of other two boolean conditions. */
final case class AndCondition[T1, T2](c1: Condition[T1], c2: Condition[T2])
    extends Condition[Unit] with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Unit] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (Some(_), Some(_)) => Some(())
    case _ => None
  }
}

/** A boolean condition that evaluates to the logic-or of other two boolean conditions. */
case class OrCondition[T1, T2](c1: Condition[T1], c2: Condition[T2])
    extends Condition[Unit] with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Unit] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (Some(_), _) => Some(())
    case (_, Some(_)) => Some(())
    case _ => None
  }
}

/** A boolean condition that evaluates to the logic-xor of other two boolean conditions. */
case class XorCondition[T1, T2](c1: Condition[T1], c2: Condition[T2])
    extends Condition[Unit] with BinaryEventConditioned {

  override def eval(ctx: EvalContext): Option[Unit] = (c1.eval(ctx), c2.eval(ctx)) match {
    case (None, Some(_)) => Some(())
    case (Some(_), None) => Some(())
    case _ => None
  }
}

/** A condition that evaluates another condition when a boolean condition is met. */
case class WhenCondition[T1, T2](c1: Condition[T1], c2: Condition[T2])
    extends Condition[T1] with BinaryEventConditioned {

  def eval(ctx: EvalContext): Option[T1] = c2.eval(ctx) match {
    case Some(x) => c1.eval(ctx)
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
  * A condition watcher wraps a sequence of pairs condition-action with the purpose be aware when
  * any of these conditions is met. Upon creation, it retrieves the set of devices whose events
  * each condition depends on. Then it subscribes to the event channel for any event on these
  * devices. When one of these events is received, the conditions are evaluated. If the evaluation
  * is positive, the action associated to it is executed.
  *
  * @param eventChannel The event channel to watch for condition inputs.
  * @param watched A sequence of pairs of condition-action to be watched
  * @param whenNotMet An action to be executed when none of the conditions are met
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

  def not[T](cond: Condition[T]): Condition[Unit] = new NotCondition(cond)

  def watch[T](watched: (Condition[T], T => Unit)*)(whenNotMet: => Unit) {
    new ConditionWatcher(eventChannel)(watched: _*)(whenNotMet)
  }

  def watch[T](cond: Condition[T]*)(whenMet: T => Unit)(whenNotMet: => Unit) {
    watch(cond.map(c => (c, whenMet)): _*)(whenNotMet)
  }

  def eventMatch[T](dev: DeviceId, pred: PartialFunction[Any, Option[T]]): Condition[T] =
    new EventMatchCondition[T](dev)(pred)

  def deviceIsInitialized(dev: DeviceId): Condition[DeviceId] = eventMatch(dev, {
    case StateChangedEvent(None, _) => Some(dev)
    case _ => None
  })

  def deviceStateChanged[State : Manifest](dev: DeviceId): Condition[(DeviceId, State)] =
    EventMatchCondition[(DeviceId, State)](dev) {
      case StateChangedEvent(_, s: State) => Some(dev -> s)
      case _ => None
    }

  def deviceIs[State](dev: DeviceId, state: State): Condition[DeviceId] = eventMatch(dev, {
    case StateChangedEvent(_, `state`) => Some(dev)
    case _ => None
  })

  def deviceIs[State : Manifest, T](dev: DeviceId)
                                (pred: PartialFunction[State, Option[T]]): Condition[T] =
    eventMatch(dev, {
      case StateChangedEvent(_, s: State) => pred(s)
      case _ => None
    })
}
