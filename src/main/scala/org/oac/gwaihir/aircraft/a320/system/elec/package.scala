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
  val AcBusOneTieContId = AcSubsystemId / "Bus1TieCont"
  val AcBusTwoId = AcSubsystemId / "Bus2"
  val AcBusTwoTieContId = AcSubsystemId / "Bus2TieCont"
  val AcEssFeedSwitchId = AcSubsystemId / "EssFeedSwitch"
  val AcEssFeedNormContactorId = AcSubsystemId / "EssFeedNormCont"
  val AcEssFeedAltContactorId = AcSubsystemId / "EssFeedAltCont"
  val ApuGenId = AcSubsystemId / "ApuGen"
  val ApuGenContId = AcSubsystemId / "ApuGenCont"
  val GenOneId = AcSubsystemId / "Gen1"
  val GenOneContId = AcSubsystemId / "Gen1Cont"
  val GenTwoId = AcSubsystemId / "Gen2"
  val GenTwoContId = AcSubsystemId / "Gen2Cont"
  val ExtPowerId = AcSubsystemId / "ExtPower"
  val ExtPowerContId = AcSubsystemId / "ExtPowerCont"
  val TrOneId = AcSubsystemId / "Tr1"
  val TrOneContactorId = AcSubsystemId / "Tr1Cont"
  val TrTwoId = AcSubsystemId / "Tr2"
  val TrTwoContactorId = AcSubsystemId / "Tr2Cont"

  val BatteryOneId = DcSubsystemId / "Battery1"
  val BatteryOneContId = DcSubsystemId / "Battery1Cont"
  val BatteryTwoId = DcSubsystemId / "Battery2"
  val BatteryTwoContId = DcSubsystemId / "Battery2Cont"
  val DcBatteryBusId = DcSubsystemId / "BatteryBus"
  val DcBusOneId = DcSubsystemId / "Bus1"
  val DcBusTwoId = DcSubsystemId / "Bus2"
  val DcTieOneContId = DcSubsystemId / "Tie1Cont"
  val DcTieTwoContId = DcSubsystemId / "Tie2Cont"
  val HotBusOneId = DcSubsystemId / "HotBus1"
  val HotBusTwoId = DcSubsystemId / "HotBus2"
}
