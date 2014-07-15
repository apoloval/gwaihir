package gwaihir.core.rx

/** A pull-consumer stream of data.
  *
  * The [[RxStream]] class represents a pull-consumer stream of data. Pull-consumer means that
  * the data is obtained by pulling values from the stream. It is possible to register
  * listeners that will be invoked when a new value is available in the stream.
  *
  * @tparam A The type of the elements produced by the stream.
  */
trait RxStream[+A] {

  /** Pull the currently available element from the stream.
    *
    * Obtain the currenly available element from the stream. Subsequent calls to this function
    * will not retrieve a new value unless it is produced among the two calls. In order to know
    * when a new value is available, use [[onAvailable()]]
    *
    * @return The currently available element from this stream.
    */
  def get: A

  /** Add a new listener to this stream.
    *
    * Adds a new listener that will be invoked when a new value is available in this stream.
    *
    * @param listener A function that receives the stream as argument
    */
  def onAvailable(listener: this.type => Unit): Unit

  /** Add a new listener to this stream.
    *
    * Adds a new listener as [[onAvailable(Listener)]] does, but using a by-name action instead
    * of a `Stream => Unit` function.
    *
    * @param action An action to be invoked when the stream is ready to produce a new value
    */
  def onAvailable(action: => Unit): Unit = {
    onAvailable(_ => action)
  }
}
