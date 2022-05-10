package util

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import user._


object Implicits {

  implicit def cast[A](cio: ConnectionIO[A])
                      (implicit xa: Aux[IO, Unit]): IO[A] =
    cio.transact(xa)

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
