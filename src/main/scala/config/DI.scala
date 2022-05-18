package config

import cats.data._
import cats.effect._
import doobie._
import doobie.util.transactor.Transactor.Aux
import moderator.ModeratorRouter.moderatorRouter
import moderator.{DBModeratorRepository, ModeratorRepository, ModeratorService}
import org.http4s.server.Router
import org.http4s.{Request, Response}
import session.{DBSessionRepository, SessionRepository}
import student.StudentRouter.studentRouter
import student.{DBStudentRepository, StudentRepository, StudentService}
import teacher.TeacherRouter.teacherRouter
import teacher.{DBTeacherRepository, TeacherRepository, TeacherService}
import user.UserRouter.userRouter
import user.{DBUserRepository, UserRepository, UserService}


object DI {

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "user",
    "password"
  )

  val studentRep: StudentRepository = new DBStudentRepository(xa)
  val teacherRep: TeacherRepository = new DBTeacherRepository(xa)
  val userRep: UserRepository = new DBUserRepository(xa)
  val sessionRep: SessionRepository = new DBSessionRepository(xa)
  val moderatorRep: ModeratorRepository = new DBModeratorRepository(xa)


  val userService: UserService = new UserService(userRepository = userRep, sessionRepository = sessionRep)
  val teacherService: TeacherService = new TeacherService(teacherRepository = teacherRep,
    userRepository = userRep, sessionRepository = sessionRep)
  val studentService: StudentService = new StudentService(userRepository = userRep, studentRepository = studentRep,
    sessionRepository = sessionRep, teacherRepository = teacherRep)
  val moderatorService: ModeratorService = new ModeratorService(moderatorRep, sessionRep)

  val httpResource: Resource[IO, Kleisli[IO, Request[IO], Response[IO]]] =
    Resource.pure(
      Router[IO](
        mappings =
          "api/user" -> userRouter(userService),
          "api/teacher" -> teacherRouter(teacherService),
          "api/student" -> studentRouter(studentService),
          "api/moderator" -> moderatorRouter(moderatorService)
      ).orNotFound
    )
}
