package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class LogicalBindingsTest extends FlatSpec with Matchers with LogicalBindings {

  "Logical bindings" must "bind with not operator" in {
    val p1 = Property(true)
    val p2 = Property(!p1)
    p2.get should be (false)

    p1.set(false)
    p2.get should be (true)
  }

  it must "bind with and operator" in {
    val p1 = Property(true)
    val p2 = Property(false)
    val p3 = Property(p1 && p2)
    p3.get should be (false)

    p2.set(true)
    p3.get should be (true)
  }

  it must "bind with or operator" in {
    val p1 = Property(true)
    val p2 = Property(false)
    val p3 = Property(p1 || p2)
    p3.get should be (true)

    p1.set(false)
    p3.get should be (false)
  }
}
