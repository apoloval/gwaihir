package gwaihir.core.rx

import org.scalatest.{Matchers, FlatSpec}

class SelectionBindingsTest extends FlatSpec with Matchers with SelectionBindings {

  "Selection binding" must "bind with select" in {
    val p1: Property[Option[Int]] = Property(Some(1))
    val p2: Property[Option[Int]] = Property(Some(2))
    val p3 = Property(select(p1, p2))
    p3.get should be (Some(1))

    p1.set(None)
    p3.get should be (Some(2))

    p2.set(None)
    p3.get should be (None)
  }

  it must "bindMap with select" in {
    val p1: Property[Option[Int]] = Property(Some(1))
    val p2: Property[Option[Int]] = Property(Some(2))
    val p3 = Property(selectMap(p1, p2) { i => i.toString })
    p3.get should be (Some("1"))

    p1.set(None)
    p3.get should be (Some("2"))

    p2.set(None)
    p3.get should be (None)
  }
}
