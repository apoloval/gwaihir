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

import org.scalatest.{Matchers, FlatSpec}

class DeviceIdTest extends FlatSpec with Matchers {

  "Device ID" must "be constructible from a stringfied path using separator" in {
    val id = DeviceId("/elec/ac/bus1")
    id.toString should be ("/elec/ac/bus1")
    id.parent.get should be (DeviceId("/elec/ac"))
    id.parent.get.parent.get should be (DeviceId("/elec"))
  }

  it must "construct as root device ID for separator" in {
    DeviceId("/") should be (RootDeviceId)
  }

  it must "construct as root device ID empty string" in {
    DeviceId("") should be (RootDeviceId)
  }

  it must "contain a child device" in {
    DeviceId("/elec/ac").contains(DeviceId("/elec/ac/bus1")) should be (true)
  }

  it must "not contain a non-child device" in {
    DeviceId("/elec/ac").contains(DeviceId("/elec/dc/ess-bus")) should be (false)
  }

  it must "contain itself" in {
    DeviceId("/elec/ac").contains(DeviceId("/elec/ac")) should be (true)
  }

  it must "belong to an ancestor" in {
    DeviceId("/elec/ac/bus1").belongsTo(DeviceId("/elec")) should be (true)
  }

  it must "not belong to a non ancestor" in {
    DeviceId("/elec/ac/bus1").belongsTo(DeviceId("/hyd")) should be (false)
  }

  it must "belong to itself" in {
    DeviceId("/elec/ac/bus1").belongsTo(DeviceId("/elec/ac/bus1")) should be (true)
  }

  "Root device ID" must "be converted into string as a single separator" in {
    RootDeviceId.toString should be (DeviceId.Separator)
  }

  it must "have no parent" in {
    RootDeviceId.parent should not be ('defined)
  }

  it must "contain any other device" in {
    RootDeviceId.contains(DeviceId("/a/b/c/d")) should be (true)
    RootDeviceId.contains(DeviceId("/1/2/3/4")) should be (true)
  }

  it must "belong only to itself" in {
    RootDeviceId.belongsTo(RootDeviceId) should be (true)
    RootDeviceId.belongsTo(DeviceId("/a/b/c/d")) should be (false)
  }
}
