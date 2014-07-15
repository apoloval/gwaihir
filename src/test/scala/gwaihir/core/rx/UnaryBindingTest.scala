package gwaihir.core.rx

import org.scalatest.{FlatSpec, Matchers}

class UnaryBindingTest extends FlatSpec with Matchers {

  "Unary binding" must "propagate values downstream" in {
    val p1 = Property("foobar")
    val p2 = Property(UnaryBinding(p1) { s => s.size })

    p2.get should be (6)
    p1.set("Hello World!")
    p2.get should be (12)
  }
}
