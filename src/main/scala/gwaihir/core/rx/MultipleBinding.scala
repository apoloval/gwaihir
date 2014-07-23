package gwaihir.core.rx

/** A binding to multiple streams.
  *
  * This type of binding represents a stream that produces its values by applying a function
  * to the values consumed from a sequence of upstreams.
  *
  * @param upstreams The streams whose values are consumed, applied with f and propagated downstream.
  * @param f The function applied to the values of upstreams to produce values downstream.
  * @tparam A The type of the elements produced by the upstreams.
  * @tparam B The type of the elements propagated downstream
  */
class MultipleBinding[+A, B](upstreams: Seq[RxStream[A]], f: Seq[A] => B) extends RxStream[B] {

  private val listeners = new ListenersHelper[this.type]

  override def get = f(upstreams.map(_.get))

  override def onAvailable(listener: (this.type) => Unit) = listeners.addListener(listener)

  upstreams.foreach(_.onAvailable { listeners.invokeListeners(this) })
}

object MultipleBinding {

  /** Create a new multiple binding.
    *
    * @param upstreams The upstreams to consume
    * @param f The function to apply to upstream values
    * @tparam A The type of the upstream values
    * @tparam B The type of downstream values
    * @return The new binding
    */
  def apply[A, B](upstreams: RxStream[A]*)
                 (f: Seq[A] => B): MultipleBinding[A, B] =
    new MultipleBinding[A, B](upstreams, f)
}
