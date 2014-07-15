package gwaihir.core.rx

/** A binding to a single stream.
  *
  * This type of binding represents a stream that produces its values by applying a function
  * to the values consumed from a single upstream.
  *
  * @param upstream The stream whose values are consumed, applied with f and propagated downstream.
  * @param f The function applied to the values of upstream to produce values downstream.
  * @tparam A The type of the elements produced by the upstream.
  * @tparam B The type of the elements propagated downstream
  */
class UnaryBinding[A, B](upstream: RxStream[A], f: A => B) extends RxStream[B] {

  private val listeners = new ListenersHelper[this.type]

  override def get = f(upstream.get)

  override def onAvailable(listener: (this.type) => Unit) = listeners.addListener(listener)

  upstream.onAvailable { listeners.invokeListeners(this) }
}

object UnaryBinding {

  /** Create a new unary binding.
    *
    * @param upstream The upstream to consume
    * @param f The function to apply to upstream values
    * @tparam A The type of upstream values
    * @tparam B The type of downstream values
    * @return The new binding
    */
  def apply[A, B](upstream: RxStream[A])(f: A => B): UnaryBinding[A, B] =
    new UnaryBinding[A, B](upstream, f)
}
