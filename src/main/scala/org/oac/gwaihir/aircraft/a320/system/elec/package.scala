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

package org.oac.gwaihir.aircraft.a320.system

import org.oac.gwaihir.core.DeviceId

package object elec {

  val Id = DeviceId("/System/Elec")
  val AcSubsystemId = Id / "Ac"
  val DcSubsystemId = Id / "Dc"
  val PanelSubsystemId = Id / "Panel"

  val AcBusOneId = AcSubsystemId / "Bus1"
  val AcBusTwoId = AcSubsystemId / "Bus2"
  val AcEssBusId = AcSubsystemId / "EssBus"
  val ApuGenId = AcSubsystemId / "ApuGen"
  val EmerGenId = AcSubsystemId / "EmerGen"
  val EssTrId = AcSubsystemId / "EssTr"
  val ExtPowerId = AcSubsystemId / "ExtPower"
  val TrOneId = AcSubsystemId / "Tr1"
  val GenOneId = AcSubsystemId / "Gen1"
  val GenTwoId = AcSubsystemId / "Gen2"
  val TrTwoId = AcSubsystemId / "Tr2"

  val BatteryOneId = DcSubsystemId / "Battery1"
  val BatteryTwoId = DcSubsystemId / "Battery2"
  val DcBatteryBusId = DcSubsystemId / "BatteryBus"
  val DcBusOneId = DcSubsystemId / "Bus1"
  val DcBusTwoId = DcSubsystemId / "Bus2"
  val DcEssBusId = DcSubsystemId / "EssBus"
  val DcEssTrId = DcSubsystemId / "EssTr"
  val HotBusOneId = DcSubsystemId / "HotBus1"
  val HotBusTwoId = DcSubsystemId / "HotBus2"

  val AcBusTieButtonId = PanelSubsystemId / "BusTieBtn"
  val AcEssFeedButtonId = PanelSubsystemId / "EssFeedBtn"
}
