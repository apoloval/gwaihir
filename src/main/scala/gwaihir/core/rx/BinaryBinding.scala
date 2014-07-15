package gwaihir.core.rx

/** A binding to a pair of streams.
  *
  * This type of binding represents a stream that produces its values by applying a function
  * to the values consumed from a pair of upstreams.
  *
  * @param upstream The streams whose values are consumed, applied with f and propagated downstream.
  * @param f The function applied to the values of upstream to produce values downstream.
  * @tparam A The type of the elements of the first upstream.
  * @tparam B The type of the elements of the second upstream.
  * @tparam C The type of the elements propagated downstream
  */
class BinaryBinding[A, B, C](upstream: (RxStream[A], RxStream[B]), f: (A, B) => C) extends RxStream[C] {

  private val listeners = new ListenersHelper[this.type]

  override def get = f(upstream._1.get, upstream._2.get)

  override def onAvailable(listener: (this.type) => Unit) = listeners.addListener(listener)

  Seq(upstream._1, upstream._2).foreach(_.onAvailable { listeners.invokeListeners(this) })
}

object BinaryBinding {

  /** Create a new binary binding.
    *
    * @param u1 The left upstream to consume
    * @param u2 The right upstream to consume
    * @param f The function to apply to upstream values
    * @tparam A The type of the first upstream values
    * @tparam B The type of the second upstream values
    * @tparam C The type of downstream values
    * @return The new binding
    */
  def apply[A, B, C](u1: RxStream[A], u2: RxStream[B])(f: (A, B) => C): BinaryBinding[A, B, C] =
    new BinaryBinding[A, B, C]((u1, u2), f)
}
