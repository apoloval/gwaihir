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
  with SwitchConditions { self: ConditionEvaluator => }

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
    val busTieContactor = newDevice(new BusTieContactor())
    val busTwo = newDevice(new AcBusTwo())
    val extPower = newDevice(new ExtPower())
    val extPowerContactor = newDevice(new ExtPowerContactor())
    val genOne = newDevice(new GenOne())
    val genOneContactor = newDevice(new GenOneContactor())
    val genTwo = newDevice(new GenTwo())
    val genTwoContactor = newDevice(new GenTwoContactor())
    val trOne = newDevice(new TrOne())
    val trTwo = newDevice(new TrTwo())
  }

  val panel = new DeviceSystem {

    override implicit val ctx = ElectricalSystem.this.ctx
    override val id = PanelSubsystemId

    val acEssFeedSwitch = newDevice(new Switch(AcEssFeedSwitchId))
  }

  override def init() {
    ac.init()
    panel.init()
  }
}

object ElectricalSystem {

  val Id = DeviceId("/System/Elec")
  val AcSubsystemId = Id / "Ac"
  val PanelSubsystemId = Id / "Panel"

  val AcBusOneId = AcSubsystemId / "AcBus1"
  val AcBusTwoId = AcSubsystemId / "AcBus2"
  val AcEssFeedSwitchId = AcSubsystemId / "AcEssFeedSwitch"
  val AcEssFeedNormContactorId = AcSubsystemId / "AcEssFeedNormCont"
  val AcEssFeedAltContactorId = AcSubsystemId / "AcEssFeedAltCont"
  val ApuGenId = AcSubsystemId / "ApuGen"
  val ApuGenContId = AcSubsystemId / "ApuGenCont"
  val BusTieContId = AcSubsystemId / "BusTieCont"
  val GenOneId = AcSubsystemId / "Gen1"
  val GenOneContId = AcSubsystemId / "Gen1Cont"
  val GenTwoId = AcSubsystemId / "Gen2"
  val GenTwoContId = AcSubsystemId / "Gen2Cont"
  val ExtPowerId = AcSubsystemId / "ExtPower"
  val ExtPowerContId = AcSubsystemId / "ExtPowerCont"
  val TrOneId = AcSubsystemId / "Tr1"
  val TrTwoId = AcSubsystemId / "Tr2"
}
