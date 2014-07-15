package gwaihir.core.rx

import org.scalatest.{FlatSpec, Matchers}

class BinaryBindingTest extends FlatSpec with Matchers {

  "Binary binding" must "propagate values downstream" in {
    val p1 = Property("foo")
    val p2 = Property("bar")
    val p3 = Property(BinaryBinding(p1, p2) { (s1, s2) => s1 + s2 })

    p3.get should be ("foobar")
    p1.set("milli")
    p3.get should be ("millibar")
  }
}
