package util

object Implicits {
  implicit def stringToUserType(string: String): UserType =
    string match {
      case "StudentType" => StudentType
      case "TeacherType" => TeacherType
      case "ModeratorType" => ModeratorType
    }

  implicit def stringToSex(string: String): Sex = {
    string match {
      case "Male" => Male
      case "Female" => Female
    }
  }
}
