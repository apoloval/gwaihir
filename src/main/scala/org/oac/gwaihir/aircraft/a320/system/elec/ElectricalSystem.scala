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

package org.oac.gwaihir.aircraft.a320.system.elec

import org.oac.gwaihir.aircraft.a320.common.{SwitchConditions, Switch}
import org.oac.gwaihir.core._

trait ElectricalSystemConditions
  extends ContactorConditions
  with GeneratorConditions
  with BusConditions
  with SwitchConditions
  with TransformerRectifierConditions { self: ConditionEvaluator => }

class ElectricalSystem(implicit val ctx: SimulationContext) extends Device {

  import ElectricalSystem._

  override val id = Id

  val ac = new DeviceSystem {

    override implicit val ctx = ElectricalSystem.this.ctx
    override val id = AcSubsystemId

    val acEssFeedNormContactor = newDevice(new AcEssFeedNormContactor())
    val acEssFeedAltContactor = newDevice(new AcEssFeedAltContactor())
    val apuGen = newDevice(new ApuGen())
    val apuGenContactor = newDevice(new ApuGenContactor())
    val busOne = newDevice(new AcBusOne())
    val busOneTieContactor = newDevice(new AcBusOneTieContactor())
    val busTwo = newDevice(new AcBusTwo())
    val busTwoTieContactor = newDevice(new AcBusTwoTieContactor())
    val extPower = newDevice(new ExtPower())
    val extPowerContactor = newDevice(new ExtPowerContactor())
    val genOne = newDevice(new GenOne())
    val genOneContactor = newDevice(new GenOneContactor())
    val genTwo = newDevice(new GenTwo())
    val genTwoContactor = newDevice(new GenTwoContactor())
    val trOne = newDevice(new TrOne())
    val trTwo = newDevice(new TrTwo())
  }

  val dc = new DeviceSystem {

    override implicit val ctx = ElectricalSystem.this.ctx
    override val id = DcSubsystemId

    val busOne = newDevice(new DcBusOne())
    val busTwo = newDevice(new DcBusTwo())
    val tieOneContactor = newDevice(new DcTieOneContactor())
    val tieTwoContactor = newDevice(new DcTieTwoContactor())
    val trOneContactor = newDevice(new TrOneContactor())
    val trTwoContactor = newDevice(new TrTwoContactor())
  }

  val panel = new DeviceSystem {

    override implicit val ctx = ElectricalSystem.this.ctx
    override val id = PanelSubsystemId

    val acEssFeedSwitch = newDevice(new Switch(AcEssFeedSwitchId))
  }

  override def init() {
    ac.init()
    dc.init()
    panel.init()
  }
}

object ElectricalSystem {

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
  val BatteryTwoId = DcSubsystemId / "Battery2"
  val DcBusOneId = DcSubsystemId / "Bus1"
  val DcBusTwoId = DcSubsystemId / "Bus2"
  val DcTieOneContId = DcSubsystemId / "Tie1Cont"
  val DcTieTwoContId = DcSubsystemId / "Tie2Cont"
}
