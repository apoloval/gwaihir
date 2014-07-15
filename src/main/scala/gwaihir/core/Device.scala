package gwaihir.core

/** A simulated device. */
trait Device {

  def name: String

  protected def propName(subname: String) = s"$subname/$subname"
}
