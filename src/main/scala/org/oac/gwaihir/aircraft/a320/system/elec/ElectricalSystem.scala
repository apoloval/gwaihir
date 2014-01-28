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
  extends GeneratorConditions
  with BusConditions
  with SwitchConditions
  with TransformerRectifierConditions { self: ConditionEvaluator => }

class ElectricalSystem(implicit val ctx: SimulationContext) extends DeviceSystem {

  override val id = Id

  // Generators
  val genOne = newDevice(new GenOne())
  val genTwo = newDevice(new GenTwo())
  val apuGen = newDevice(new ApuGen())
  val extPower = newDevice(new ExtPower())
  val emerGen = newDevice(new EmerGen())

  // Buses
  val acBusOne = newDevice(new AcBusOne())
  val acBusTwo = newDevice(new AcBusTwo())
  val acEssBus = newDevice(new AcEssBus())
  val dcBusOne = newDevice(new DcBusOne())
  val dcBusTwo = newDevice(new DcBusTwo())
  val dcBatBus = newDevice(new DcBatteryBus())
  val dcEssBus = newDevice(new DcEssentialBus())

  // Transformer rectifiers (TRs)
  val trOne = newDevice(new TrOne())
  val trTwo = newDevice(new TrTwo())
  val essTr = newDevice(new EssTr())

  // Panel 
  val acEssFeedSwitch = newDevice(new AcEssFeedSwitch())
  val acBusTieSwitch = newDevice(new AcBusTieSwitch())
}
