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

class EventChannelTest extends FlatSpec with Matchers {

  "Event channel" must "subscribe and send events" in new FreshChannel {
    chan.subscribe(acGen1)(observe)
    chan.send(acGen1, devRunning)
    lastSender should be (Some(acGen1))
    lastEvent should be (Some(devRunning))
  }

  it must "send events to subscription on a sender ancestor" in new FreshChannel {
    chan.subscribe(ac)(observe)
    chan.send(acGen1, devRunning)
    lastSender should be (Some(acGen1))
    lastEvent should be (Some(devRunning))
  }

  it must "not send events to subscription on a non-sender ancestor" in new FreshChannel {
    chan.subscribe(ac)(observe)
    chan.send(dcEssBus, devRunning)
    lastSender should be (None)
    lastEvent should be (None)
  }

  it must "not send anything when no subscription is done" in new FreshChannel {
    chan.send(dcEssBus, devRunning)
    lastSender should be (None)
    lastEvent should be (None)
  }

  case class DummyEvent(msg: String)

  trait CallbackUtils {
    var lastSender: Option[DeviceId] = None
    var lastEvent: Option[DummyEvent] = None
    val observe: PartialFunction[(DeviceId, Any), Unit] = {
      case (sender: DeviceId, event: DummyEvent) =>
        lastSender = Some(sender)
        lastEvent = Some(event)
    }
  }

  trait SampleDevices {
    val ac = DeviceId("/elec/ac")
    val acGen1 = DeviceId("/elec/ac/gen-1")
    val dcEssBus = DeviceId("/elec/dc/ess-bus")
  }

  trait SampleEvents {
    val devRunning = DummyEvent("running")
  }

  trait FreshChannel extends CallbackUtils with SampleDevices with SampleEvents {
    var chan = EventChannel()
  }
}
