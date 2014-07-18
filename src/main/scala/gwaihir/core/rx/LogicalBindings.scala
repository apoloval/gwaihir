package gwaihir.core.rx

trait LogicalBindings {

  implicit class BooleanStreamPimp(stream: RxStream[Boolean]) {

    def unary_not : UnaryBinding[Boolean, Boolean] =
      UnaryBinding(stream) { a => !a }

    def and(other: RxStream[Boolean]): BinaryBinding[Boolean, Boolean, Boolean] =
      BinaryBinding(stream, other) { (a, b) => a && b }

    def or(other: RxStream[Boolean]): BinaryBinding[Boolean, Boolean, Boolean] =
      BinaryBinding(stream, other) { (a, b) => a || b }

    def unary_! = unary_not
    def &&(other: RxStream[Boolean]) = and(other)
    def ||(other: RxStream[Boolean]) = or(other)
  }
}

object LogicalBindings extends LogicalBindings
