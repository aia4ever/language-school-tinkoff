package util

sealed trait UserType

case object Student extends UserType

case object Teacher extends UserType

case object Moderator extends UserType