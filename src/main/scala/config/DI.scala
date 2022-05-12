package config

import cats.data._
import cats.effect._
import doobie._
import doobie.util.transactor.Transactor.Aux
import lesson.{DBLessonRepository, LessonRepository}
import org.http4s.server.Router
import org.http4s.{Request, Response}
import session.{DBSessionRepository, SessionRepository, SessionService}
import student.StudentRouter.studentRouter
import student.{DBStudentRepository, StudentRepository, StudentService}
import teacher.TeacherRouter.teacherRouter
import teacher.{DBTeacherRepository, TeacherRepository, TeacherService}
import user.UserRouter.userRouter
import user._


object DI {

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/postgres",
    "user",
    "password"
  )

  val studentRep: StudentRepository = new DBStudentRepository
  val teacherRep: TeacherRepository = new DBTeacherRepository
  val userRep: UserRepository = new DBUserRepository
  val sessionRep: SessionRepository = new DBSessionRepository
  val lessonRep: LessonRepository = new DBLessonRepository


  val userService: UserService = new UserService(xa, userRep, sessionRep)
  val teacherService: TeacherService = new TeacherService(xa, teacherRep, lessonRep)
  val sessionService: SessionService = new SessionService(xa, sessionRep)
  val studentService: StudentService = new StudentService(xa, studentRep, lessonRep)

  val httpResource: Resource[IO, Kleisli[IO, Request[IO], Response[IO]]] =
    Resource.pure(
      Router[IO](
        mappings =
          "api/user" -> userRouter(userService),
          "api/teacher" -> teacherRouter(teacherService, sessionService),
          "api/student" -> studentRouter(studentService, sessionService, teacherService)
      ).orNotFound
    )
}
