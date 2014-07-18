package gwaihir.core.rx

trait ComparisonBindings {

  implicit class AnyPimp[A](stream: RxStream[A]) {

    def equalsTo(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => a == b }

    def notEqualsTo(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => a != b }
  }

  implicit class OrderingStreamPimp[A : Ordering](stream: RxStream[A]) {

    private val order = implicitly[Ordering[A]]

    def lessThan(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => order.lt(a, b) }

    def lessThanOrEqual(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => order.lteq(a, b) }

    def greaterThan(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => order.gt(a, b) }

    def greaterThanOrEqual(other: RxStream[A]): BinaryBinding[A, A, Boolean] =
      BinaryBinding(stream, other) { (a, b) => order.gteq(a, b) }

    def < (other: RxStream[A]) = lessThan(other)
    def <= (other: RxStream[A]) = lessThanOrEqual(other)
    def > (other: RxStream[A]) = greaterThan(other)
    def >= (other: RxStream[A]) = greaterThanOrEqual(other)
  }
}

object ComparisonBindings extends ComparisonBindings
