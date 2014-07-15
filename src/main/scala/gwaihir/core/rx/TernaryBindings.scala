package gwaihir.core.rx

/** A binding to a ternary tuple of streams.
  *
  * This type of binding represents a stream that produces its values by applying a function
  * to the values consumed from a ternary tuple of upstreams.
  *
  * @param upstream The streams whose values are consumed, applied with f and propagated downstream.
  * @param f The function applied to the values of upstream to produce values downstream.
  * @tparam A The type of the elements produced by the first upstream.
  * @tparam B The type of the elements produced by the second upstream.
  * @tparam C The type of the elements produced by the third upstream.
  * @tparam D The type of the elements propagated downstream
  */
class TernaryBinding[A, B, C, D](upstream: (RxStream[A], RxStream[B], RxStream[C]), f: (A, B, C) => D)
    extends RxStream[D] {

  private val listeners = new ListenersHelper[this.type]

  override def get = f(upstream._1.get, upstream._2.get, upstream._3.get)

  override def onAvailable(listener: (this.type) => Unit) = listeners.addListener(listener)

  Seq(upstream._1, upstream._2, upstream._3).foreach(_.onAvailable {
    listeners.invokeListeners(this)
  })
}

object TernaryBinding {

  /** Create a new ternary binding.
    *
    * @param u1 The first upstream to consume
    * @param u2 The second upstream to consume
    * @param u3 The third upstream to consume
    * @param f The function to apply to upstream values
    * @tparam A The type of the first upstream values
    * @tparam B The type of the second upstream values
    * @tparam C The type of the third upstream values
    * @tparam D The type of downstream values
    * @return The new binding
    */
  def apply[A, B, C, D](u1: RxStream[A], u2: RxStream[B], u3: RxStream[C])
                       (f: (A, B, C) => D): TernaryBinding[A, B, C, D] =
    new TernaryBinding[A, B, C, D]((u1, u2, u3), f)
}
