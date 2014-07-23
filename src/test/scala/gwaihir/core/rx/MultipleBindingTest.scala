package gwaihir.core.rx

import org.scalatest.{FlatSpec, Matchers}

class MultipleBindingTest extends FlatSpec with Matchers {

  "Multiple binding" must "propagate values downstream" in {
    val p1 = Property(7)
    val p2 = Property(1)
    val p3 = Property(5)
    val p4 = Property(MultipleBinding(p1, p2, p3) { nums => nums.sum })
    p4.get should be (13)

    p1.set(1)
    p4.get should be (7)
  }
}
