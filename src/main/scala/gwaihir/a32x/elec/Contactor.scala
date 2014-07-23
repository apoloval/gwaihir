package gwaihir.a32x.elec

import gwaihir.core.Device
import gwaihir.core.rx._
import gwaihir.units.{Current, ZeroCurrent}

class Contactor(override val name: String) extends Device with ConditionalBindings {

  val isOpen: BooleanProperty = Property(propName("isOpen"), true)
  val input: Property[Current] = Property(propName("input"), ZeroCurrent)
  val output: ReadOnlyProperty[Current] = Property(
    propName("output"), when(isOpen) then Constant(ZeroCurrent) otherwise input)

  def open(): Unit = { isOpen.set(true) }
  def close(): Unit = { isOpen.set(false) }
}
