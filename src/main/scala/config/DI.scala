package config

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import org.http4s.server.Router
import org.http4s.{Request, Response}
import session.SessionService
import teacher.TeacherRouter.teacherRouter
import teacher.TeacherService
import user.UserRouter.userRouter
import user._


object DI {

  implicit val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "aia4ever",
    "password"
  )

  implicit val userService: UserService = new UserService
  implicit val teacherService: TeacherService = new TeacherService
  implicit val sessionService: SessionService = new SessionService

    val httpResource: Resource[IO, Kleisli[IO, Request[IO], Response[IO]]] = Resource.pure(Router[IO](mappings = "api/user" -> userRouter, "api/teacher" -> teacherRouter).orNotFound)
}
