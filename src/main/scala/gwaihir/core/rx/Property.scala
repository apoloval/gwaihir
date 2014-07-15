package gwaihir.core.rx

import gwaihir.core.TaskExecutor

/** A reactive property of type T.
  *
  * This class encapsulates a value of type T in a reactive property. Reactive properties are
  * values that can react when they are modified. Such reaction involves a listener that is
  * invoked when such modification takes place. Typically, reactive properties are bound among
  * them so a given property is automatically updated when its bounded peer is modified.
  *
  * @param initialValue The initial value encapsulated by the reactive property
  * @tparam A The type of the value that is being encapsulated.
  */
class Property[A](name: String, initialValue: A) extends RxStream[A] {

  private val listeners = new ListenersHelper[this.type]
  private var value: Option[A] = Some(initialValue)
  private var upstream: Option[RxStream[A]] = None

  override def get = value.orElse(upstream.map(_.get)).get

  override def onAvailable(listener: (this.type) => Unit) = listeners.addListener(listener)

  def bind(stream: RxStream[A]): Unit = {
    upstream = Some(stream)
    invalidateValue()
    stream.onAvailable(invalidateValue())
  }

  def isBounded: Boolean = upstream.isDefined

  /** Set a new value for this property.
    *
    * If the new value differs from the previous one, the new value replaces the current one and
    * all the callbacks from this property are invoked.
    *
    * @param newValue The new value for the property.
    */
  def set(newValue: A): Unit = {
    require(!isBounded, s"cannot set value in property $name because it is bounded")
    if (newValue != value) {
      value = Some(newValue)
      listeners.invokeListeners(this)
    }
  }

  private def invalidateValue(): Unit = { value = None }
}

object Property {

  private var nextPropId: Int = 1

  /** Create a new reactive property.
    *
    * @param name The name of the property
    * @param initialValue Its initial value
    * @tparam A The type of the property
    * @return The new property
    */
  def apply[A](name: String, initialValue: A): Property[A] =
    new Property[A](name, initialValue)

  /** Create a new reactive property with a self-assigned name.
    *
    * @param initialValue The initial value of the property
    * @tparam A The type of the property
    * @return The new property
    */
  def apply[A](initialValue: A): Property[A] =
    new Property[A](selfAssignedName(), initialValue)

  /** Create a new reactive property bounded to the given stream.
    *
    * @param name The name of the property
    * @param s The stream the property will be bounded to
    * @tparam A The type of the property
    * @return The new property
    */
  def apply[A](name: String, s: RxStream[A]): Property[A] = {
    val prop = new Property[A](name, s.get)
    prop.bind(s)
    prop
  }

  /** Create a new reactive property bounded to the given stream with a self-assigned name.
    *
    * @param s The stream the property will be bounded to
    * @tparam A The type of the property
    * @return The new property
    */
  def apply[A](s: RxStream[A]): Property[A] = {
    val prop = new Property[A](selfAssignedName(), s.get)
    prop.bind(s)
    prop
  }

  private def selfAssignedName(): String = {
    val id = nextPropId
    nextPropId += 1
    s"property-$id"
  }

}
