package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class ComparisonBindingsTest extends FlatSpec with Matchers with ComparisonBindings {

  "Comparison bindings" must "bind with equals to operator" in {
    val p1 = Property("foo")
    val p2 = Property("bar")
    val p3 = Property(p1 equalsTo p2)
    p3.get should be (false)

    p2.set("foo")
    p3.get should be (true)
  }

  it must "bind with not-equals to operator" in {
    val p1 = Property("foo")
    val p2 = Property("bar")
    val p3 = Property(p1 notEqualsTo p2)
    p3.get should be (true)

    p2.set("foo")
    p3.get should be (false)
  }

  it must "bind with less than operator" in {
    val p1 = Property(5)
    val p2 = Property(2)
    val p3 = Property(p1 < p2)
    p3.get should be (false)

    p2.set(7)
    p3.get should be (true)
  }

  it must "bind with less than or equal operator" in {
    val p1 = Property(5)
    val p2 = Property(2)
    val p3 = Property(p1 <= p2)
    p3.get should be (false)

    p2.set(7)
    p3.get should be (true)

    p2.set(5)
    p3.get should be (true)
  }

  it must "bind with greater than operator" in {
    val p1 = Property(5)
    val p2 = Property(2)
    val p3 = Property(p1 > p2)
    p3.get should be (true)

    p2.set(7)
    p3.get should be (false)
  }

  it must "bind with greater than or equal operator" in {
    val p1 = Property(5)
    val p2 = Property(2)
    val p3 = Property(p1 >= p2)
    p3.get should be (true)

    p2.set(7)
    p3.get should be (false)

    p2.set(5)
    p3.get should be (true)
  }

}
