package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class TernaryBindingTest extends FlatSpec with Matchers {

  "Ternary binding" must "propagate values downstream" in {
    val p1 = Property(true)
    val p2 = Property("foo")
    val p3 = Property("bar")
    val p4 = Property(TernaryBinding(p1, p2, p3) { (b, s1, s2) => if (b) s1 else s2 })
    p4.get should be ("foo")

    p1.set(false)
    p4.get should be ("bar")
  }
}
