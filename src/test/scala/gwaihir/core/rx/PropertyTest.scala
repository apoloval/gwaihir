package gwaihir.core.rx

import org.scalatest.{FlatSpec, Matchers}

class PropertyTest extends FlatSpec with Matchers {

  "A property" must "retrieve its initial value" in {
    val p = Property(7)
    p.get should be (7)
  }

  it must "retrieve its new value after set" in {
    val p = Property(7)
    p.set(12)
    p.get should be (12)
  }

  it must "invoke its listeners after set a new value" in {
    val p1 = Property(7)
    var p2 = Property(false)
    var p3 = Property(1.0)
    p1.onAvailable { p2.set(true) }
    p1.onAvailable { p => p3.set(p.get.toDouble) }

    p1.set(12)

    p2.get should be (true)
    p3.get should be (12.0)
  }

  it must "be bounded to another property" in {
    val p1 = Property(7)
    val p2 = Property(10)

    p1.bind(p2)

    p1.get should be (10)
    p2.set(5)
    p1.get should be (5)
  }

  it must "be instantiated from a bounded property" in {
    val p1 = Property(7)
    val p2 = Property(p1)

    p2.get should be (7)
    p1.set(1)
    p2.get should be (1)
  }

  it must "fail to set when bounded" in {
    val p1 = Property(7)
    val p2 = Property(p1)

    an [IllegalArgumentException] should be thrownBy { p2.set(1) }
  }

}
