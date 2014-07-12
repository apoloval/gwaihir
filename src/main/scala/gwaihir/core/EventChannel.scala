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

package gwaihir.core

/**
 * An channel to notify device events.
 *
 * This event channel is the mechanism used by Gwaihir to communicate simulated devices. In a
 * simulated aircraft, the state of each device typically depends on the state of other dozens of
 * devices. Modelling this relation by aggregation relationship between objects would derive in a
 * mess of references. Instead of that, the objects communicate among them using a event channel.
 * Each device would subscribe to events on those devices whose state depends on, and would send
 * any relevant event in order to other devices to be aware of its state.
 */
trait EventChannel {

  import EventChannel._

  /**
   * Subscribe to events produced by the given device or any of its children.
   *
   * The callback function will be invoked every time a event is produced by the given device or
   * any of its children.
   */
  def subscribe(id: DeviceId)(subs: Subscription)

  /**
   * Send a event produced by the given device to the channel.
   *
   * The event will be received by all subscriptions made on the sender device or any of its
   * ancestors.
   */
  def send(sender: DeviceId, event: Any)
}

/** An object able to provide a event channel. */
trait EventChannelProvider {

  def eventChannel: EventChannel
}

/**
 * Tree-like implementation of event channel.
 *
 * This is an implementation of event channel that arranges the subscriptions in a tree of channels
 * in order to maximize the event delivery performance.
 */
class DeviceTreeEventChannel(dev: DeviceId) extends EventChannel {

  private var children: Map[DeviceId, EventChannel] = Map.empty
  private var subscriptions: Set[EventChannel.Subscription] = Set.empty

  override def subscribe(id: DeviceId)(subs: EventChannel.Subscription) = {
    require(dev.contains(id), s"cannot subscribe callback for $id on a channel managing $dev")
    if (id.equals(dev)) subscriptions += subs
    else channelFor(id)
      .getOrElse(initChannelFor(id))
      .subscribe(id)(subs)
  }

  override def send(sender: DeviceId, event: Any) = {
    if (dev.contains(sender)) deliver(sender, event)
    channelFor(sender) match {
      case Some(c) => c.send(sender, event)
      case None =>
    }
  }

  private def channelFor(id: DeviceId): Option[EventChannel] = children.get(childAncestorOf(id))

  private def initChannelFor(id: DeviceId): EventChannel = {
    val child = childAncestorOf(id)
    val chan = new DeviceTreeEventChannel(child)
    children += (child -> chan)
    chan
  }

  private def childAncestorOf(id: DeviceId) = (
    id.ancestors.filter(_.belongsTo(dev)).filterNot(_.equals(dev)) :+ id
    ).head

  private def deliver(sender: DeviceId, event: Any) =
    subscriptions.foreach(_(sender, event))
}

/** A event channel that uses a delegate to send messages over a task executor
  *
  * This event channel wraps another event channel that acts as its delegate. While sending an
  * event, the channel will submit a task on a executor that will send the event using the
  * delegate. This allow the execution of event sending and reception to be decoupled, and
  * therefore eliminate the possibility of infinite loops.
  */
class TaskExecutorEventChannel(
    delegate: EventChannel,
    executor: TaskExecutor) extends EventChannel {

  override def subscribe(id: DeviceId)(subs: EventChannel.Subscription) =
    delegate.subscribe(id)(subs)

  override def send(sender: DeviceId, event: Any) = executor.submit {
    delegate.send(sender, event)
  }
}

object EventChannel {

  type Subscription = PartialFunction[(DeviceId, Any), Unit]

  def apply() = new DeviceTreeEventChannel(RootDeviceId)
}
