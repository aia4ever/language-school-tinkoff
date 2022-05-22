package config

import cats.data._
import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
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
  type ResTransactor[F[_]] = Resource[F, HikariTransactor[F]]

  private val xa: ResTransactor[IO] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/postgres",
        "user",
        "password",
        ce
      )
    } yield xa

  private val studentRep: StudentRepository = new DBStudentRepository(xa)
  private val teacherRep: TeacherRepository = new DBTeacherRepository(xa)
  private val userRep: UserRepository = new DBUserRepository(xa)
  private val sessionRep: SessionRepository = new DBSessionRepository(xa)
  private val moderatorRep: ModeratorRepository = new DBModeratorRepository(xa)


  private val userService: UserService =
    new UserService(
      userRepository = userRep,
      sessionRepository = sessionRep
    )
  private val teacherService: TeacherService =
    new TeacherService(
      userRepository = userRep,
      teacherRepository = teacherRep,
      sessionRepository = sessionRep
    )
  private val studentService: StudentService =
    new StudentService(
      userRepository = userRep,
      studentRepository = studentRep,
      sessionRepository = sessionRep,
      teacherRepository = teacherRep
    )
  private val moderatorService: ModeratorService = new ModeratorService(moderatorRep, sessionRep)

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
