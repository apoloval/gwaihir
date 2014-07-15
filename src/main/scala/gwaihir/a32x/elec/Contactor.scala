package gwaihir.a32x.elec

import gwaihir.core.Device
import gwaihir.core.rx._
import gwaihir.units.Amperes

class Contactor(override val name: String) extends Device with ConditionalBindings {

  val isOpen: BooleanProperty = Property(propName("isOpen"), true)
  val input: Property[Amperes] = Property(propName("input"), 0.0)
  val output: ReadOnlyProperty[Amperes] = Property(
    propName("output"), when(isOpen) then Constant(0.0) otherwise input)

  def open(): Unit = { isOpen.set(true) }
  def close(): Unit = { isOpen.set(false) }
}
