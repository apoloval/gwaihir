package gwaihir.units

object CurrentConversions {

  implicit class CurrentConversions[A](n: A)(implicit num: Numeric[A])
    extends squants.electro.ElectricCurrentConversions.ElectricCurrentConversions(n)(num)

}
