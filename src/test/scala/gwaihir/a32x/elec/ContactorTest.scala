package gwaihir.a32x.elec

import org.scalatest.{Matchers, FlatSpec}

import gwaihir.units.CurrentConversions._

class ContactorTest extends FlatSpec with Matchers {

  "Contactor" must "initiate open" in {
    val c = new Contactor("c")
    c.input.set(5.0 A)
    c.isOpen.get should be (true)
    c.output.get should be (0.0 A)
  }

  it must "output power when closed" in {
    val c = new Contactor("c")
    c.input.set(5.0 A)
    c.close()
    c.output.get should be (5.0 A)
  }
}
