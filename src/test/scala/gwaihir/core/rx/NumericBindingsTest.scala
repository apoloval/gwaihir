package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class NumericBindingsTest extends FlatSpec with Matchers with NumericBindings {

  "Numeric bindings" must "bind with add operator" in {
    val p1 = Property(5)
    val p2 = Property(7)
    val p3 = Property(p1 + p2)
    p3.get should be (12)

    p2.set(1)
    p3.get should be (6)
  }

  it must "bind with sub operator" in {
    val p1 = Property(5)
    val p2 = Property(7)
    val p3 = Property(p1 - p2)
    p3.get should be (-2)

    p2.set(1)
    p3.get should be (4)
  }

  it must "bind with mult operator" in {
    val p1 = Property(5)
    val p2 = Property(7)
    val p3 = Property(p1 * p2)
    p3.get should be (35)

    p2.set(1)
    p3.get should be (5)
  }

  it must "bind with max operator" in {
    val p1 = Property(5)
    val p2 = Property(7)
    val p3 = Property(p1 max p2)
    p3.get should be (7)

    p2.set(1)
    p3.get should be (5)
  }

  it must "bind with min operator" in {
    val p1 = Property(5)
    val p2 = Property(7)
    val p3 = Property(p1 min p2)
    p3.get should be (5)

    p2.set(1)
    p3.get should be (1)
  }
}
