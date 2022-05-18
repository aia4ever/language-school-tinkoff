package util

sealed trait UserType

case object StudentType extends UserType

case object TeacherType extends UserType

case object ModeratorType extends UserType