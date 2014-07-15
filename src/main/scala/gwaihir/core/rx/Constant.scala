package gwaihir.core.rx

/** A constant value that reacts as a stream.
  *
  * This class wraps a constant value that can be used as a stream. As it is constant, the stream
  * always returns the same value, and the `onAvailable()` callback will never be invoked.
  *
  * This type is specifically designed to turn a constant object into a stream that can be used
  * in bindings.
  *
  * @param value The constant value that will be wrapped as a reactive stream
  * @tparam A The type of the elements produced by the stream.
  */
case class Constant[+A](value: A) extends RxStream[A] {

  override def get = value

  override def onAvailable(listener: (this.type) => Unit) = {}
}
