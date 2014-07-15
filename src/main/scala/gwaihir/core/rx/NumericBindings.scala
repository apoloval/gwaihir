package gwaihir.core.rx

trait NumericBindings {

  implicit class NumericStreamPimp[A : Numeric](stream: RxStream[A]) {

    private val num = implicitly[Numeric[A]]

    def add(other: RxStream[A]): BinaryBinding[A, A, A] =
      BinaryBinding(stream, other) { (a, b) => num.plus(a, b) }

    def sub(other: RxStream[A]): BinaryBinding[A, A, A] =
      BinaryBinding(stream, other) { (a, b) => num.minus(a, b) }

    def mult(other: RxStream[A]): BinaryBinding[A, A, A] =
      BinaryBinding(stream, other) { (a, b) => num.times(a, b) }

    def max(other: RxStream[A]): BinaryBinding[A, A, A] =
      BinaryBinding(stream, other) { (a, b) => num.max(a, b) }

    def min(other: RxStream[A]): BinaryBinding[A, A, A] =
      BinaryBinding(stream, other) { (a, b) => num.min(a, b) }

    def + (other: RxStream[A]) = add(other)
    def - (other: RxStream[A]) = sub(other)
    def * (other: RxStream[A]) = mult(other)
  }
}

object NumericBindings extends NumericBindings
