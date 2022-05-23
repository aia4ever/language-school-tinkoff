package util

sealed trait Sex

case object Female extends Sex

case object Male extends Sex

object Sex {
  implicit def stringToSex(string: String): Sex =
    string match {
      case "Male" => Male
      case "Female" => Female
    }
}