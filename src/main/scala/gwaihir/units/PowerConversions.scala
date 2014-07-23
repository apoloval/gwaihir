package gwaihir.units

object PowerConversions {

  implicit class PowerConversions[A](n: A)(implicit num: Numeric[A])
    extends squants.energy.PowerConversions.PowerConversions(n)(num)

  implicit class PowerStringConversions(s: String)
    extends squants.energy.PowerConversions.PowerStringConversions(s)

}
