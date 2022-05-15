package util

object Implicits {
  implicit def stringToUserType(string: String): UserType =
    string match {
      case "Student" => Student
      case "Teacher" => Teacher
      case "Moderator" => Moderator
    }

  implicit def stringToSex(string: String): Sex = {
    string match {
      case "Male" => Male
      case "Female" => Female
    }
  }
}
