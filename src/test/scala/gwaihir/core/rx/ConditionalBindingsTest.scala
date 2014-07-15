package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class ConditionalBindingsTest extends FlatSpec with Matchers with ConditionalBindings {

  "Conditional bindings" must "bind with when-then-otherwise" in {
    val p1 = Property(true)
    val p2 = Property(1)
    val p3 = Property(when(p1) then p2 otherwise Constant(2))
    p3.get should be (1)

    p2.set(10)
    p3.get should be (10)

    p1.set(false)
    p3.get should be (2)
  }
}
