package config

import cats.data._
import cats.effect._
import doobie._
import doobie.util.transactor.Transactor.Aux
import org.http4s.server.Router
import org.http4s.{Request, Response}
import session.SessionService
import student.StudentRouter.studentRouter
import student.StudentService
import teacher.TeacherRouter.teacherRouter
import teacher.TeacherService
import user.UserRouter.userRouter
import user._


object DI {

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "aia4ever",
    "password"
  )

  val userService: UserService = new UserService(xa)
  val teacherService: TeacherService = new TeacherService(xa)
  val sessionService: SessionService = new SessionService(xa)
  val studentService: StudentService = new StudentService(xa)

    val httpResource: Resource[IO, Kleisli[IO, Request[IO], Response[IO]]] =
      Resource.pure(Router[IO]
        (mappings = "api/user" -> userRouter(userService),
          "api/teacher" -> teacherRouter(teacherService, sessionService),
          "api/student" -> studentRouter(studentService, sessionService, teacherService)
        ).orNotFound)
}
