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

package gwaihir

package object units {

  type Power = squants.energy.Power
  val Power = squants.energy.Power

  val Milliwatts = squants.energy.Milliwatts
  val Watts = squants.energy.Watts
  val Kilowatts = squants.energy.Kilowatts

  type Current = squants.electro.ElectricCurrent
  val Current = squants.electro.ElectricCurrent

  val Amperes = squants.electro.Amperes
  val Milliamperes = squants.electro.Milliamperes

  val ZeroCurrent = Amperes(0.0)

  @deprecated("Use new unit types instead")
  type AmpereHours = Double

  @deprecated("Use new unit types instead")
  type Amperes = Double
}
