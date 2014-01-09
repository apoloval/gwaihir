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

/**
 * The identity of a simulated device
 *
 * The identify of devices are designed in a hierarchical fashion. The most concrete device
 * is considered to be part of a more general device, which in turn may be part of a more general
 * system (e.g., the AC generator 1 belongs to AC subsystem, which in turn belongs to the electrical
 * system. This device hierarchy is
 */
trait DeviceId {

  /**
   * Retrieve the ID of the parent of this device.
   *
   * E.g., "/electrical/ac/bus1" returns "/electrical/ac" as its parent.
   */
  def parent: Option[DeviceId]

  /**
   * Indicates whether this device contains the one passed as argument.
   *
   * E.g., "/electrical/ac" contains "/electrical/ac/bus1".
   */
  def contains(dev: DeviceId): Boolean

  /**
   * Indicates whether this device is contained by the one passed as argument.
   *
   * E.g., "/electrical/ac/bus1" belongs to"/electrical/ac".
   */
  def belongsTo(dev: DeviceId): Boolean

  def / (name: String): DeviceId = ChildDeviceId(this, name)

  def ancestors: Seq[DeviceId] = parent match {
    case Some(p) => p.ancestors :+ p
    case None => Seq.empty
  }
}

object RootDeviceId extends DeviceId {

  override final val toString = "/"

  override val parent = None

  override def contains(dev: DeviceId) = true

  override def belongsTo(dev: DeviceId) = dev == this
}

case class ChildDeviceId(parentDev: DeviceId, name: String) extends DeviceId {

  override final val toString = parentDev match {
    case RootDeviceId => s"/$name"
    case _ => s"$parentDev/$name"
  }

  override def equals(any: Any): Boolean = any match {
    case ChildDeviceId(p, n) => p.equals(parentDev) && n.equals(name)
    case _ => false
  }

  override def parent: Option[DeviceId] = Some(parentDev)

  override def contains(dev: DeviceId) = dev.belongsTo(this)

  override def belongsTo(dev: DeviceId) = if (dev.equals(this)) true else parentDev.belongsTo(dev)
}

object DeviceId {

  val Separator: String = "/"

  /** Create a new device ID from its path representation. */
  def apply(path: String): DeviceId = path
    .split(Separator)
    .filterNot(_.isEmpty)
    .foldLeft[DeviceId](RootDeviceId)((a, b) => new ChildDeviceId(a, b))
}
