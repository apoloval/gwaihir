package gwaihir.core

package object rx {

  type ReadOnlyProperty[+A] = RxStream[A]

  type BooleanProperty = Property[Boolean]
  type DoubleProperty = Property[Double]
  type FloatProperty = Property[Float]
  type IntProperty = Property[Int]
  type LongProperty = Property[Long]
  type StringProperty = Property[String]
}
