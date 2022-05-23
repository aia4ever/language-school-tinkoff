package util

sealed trait UserType

case object StudentType extends UserType

case object TeacherType extends UserType

case object ModeratorType extends UserType

object UserType {
  implicit def stringToUserType(string: String): UserType =
    string match {
      case "StudentType" => StudentType
      case "TeacherType" => TeacherType
      case "ModeratorType" => ModeratorType
    }
}