package student

import cats.effect.IO
import data.req.GradeReq
import io.circe.generic.auto._
import io.circe.syntax._
import lesson.Lesson
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import session.SessionService
import teacher.{Teacher, TeacherService}
import util.Implicits._
import util.Util.{auth, rest}

object StudentRouter {
  implicit val studentEncoder: EntityEncoder[IO, Student] = jsonEncoderOf[IO, Student]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]
  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val rateReq: EntityEncoder[IO, GradeReq] = jsonEncoderOf[IO, GradeReq]


  def studentRouter(studentService: StudentService, sessionService: SessionService, teacherService: TeacherService): HttpRoutes[IO] = {
    HttpRoutes.of {

      case GET -> Root / "lessons" / LongVar(teacher) => studentService.lessonsByTeacher(teacher)
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "lesson" / LongVar(id) => studentService.lesson(id)
        .forbidden

      case req@GET -> Root / "your-lesson" / LongVar(id) =>
        (for {
          session <- auth(req)
          _id <- sessionService.checkSession(session)
          is <- studentService.isStudent(_id)
          res <- if (is) studentService.yourLesson(id, _id)
          else IO.raiseError(throw new Exception("You are not a Student"))
        } yield res).forbidden


      case req@POST -> Root / "lesson" / LongVar(id) / "sign-up"  =>
        (for {
          session <- auth(req)
          studentId <- sessionService.checkSession(session)
          is <- studentService.isStudent(studentId)
          res <- if (is) studentService.signUp(id, studentId)
          else IO.raiseError(throw new Exception("You are not a Student"))
        } yield res).forbidden


      case req@POST -> Root / "lesson" / LongVar(id) / "sign-out"  => auth(req).flatMap(sessionService.checkSession)
        .flatMap(studentService.signOut(id, _)).flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "upcoming-lessons" =>  auth(req).flatMap(sessionService.checkSession)
        .flatMap(studentService.upcoming).flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "previous-lessons"  => auth(req).flatMap(sessionService.checkSession)
        .flatMap(studentService.previous).flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "next" / session => auth(req).flatMap(sessionService.checkSession)
        .flatMap(studentService.next).forbidden

      case req@POST -> Root / "evaluate-teacher"  =>
        (for {
          session <- auth(req)
          request <- req.decodeJson[GradeReq]
          _ <- sessionService.checkSession(session)
          is <- teacherService.isTeacher(request.teacherId)
          res <- if (is) studentService.evaluateTeacherUpdate(request)
          else IO.raiseError(throw new Exception("You are trying evaluate not the teacher"))
        } yield res).forbidden

      case req@POST -> Root / "cash-in" =>
        (for {
          session <- auth(req)
          request <- req.decodeJson[Double]
          _ <- sessionService.checkSession(session)
          res <- studentService.cashIn(request)
        } yield res).forbidden

    }
  }
}