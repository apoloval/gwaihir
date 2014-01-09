/*
 * This file is part of Open Airbus Cockpit
 * Copyright (C) 2012, 2013, 2014 Alvaro Polo
 *
 * Open Airbus Cockpit is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Open Airbus Cockpit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Open Airbus
 * Cockpit. If not, see <http://www.gnu.org/licenses/>.
 */

package org.oacsd.gwaihir.core

/** An object representing an aircraft device being simulated. */
trait Device {

  /** Obtain the ID for this device. */
  def id: DeviceId

  /** Initialize the device. */
  def init()
}

/** A special case of device that conforms a system of devices. */
trait DeviceSystem extends Device {

  private var devs: Set[Device] = Set.empty

  protected def newDevice[T <: Device](dev: T) : T = {
    devs = devs + dev
    dev
  }

  override final def init() {
    devs.foreach(dev => dev.init())
  }
}