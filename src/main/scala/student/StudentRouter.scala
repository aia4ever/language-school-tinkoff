package student

import cats.effect.IO
import data.dto.{Lesson, Student, Teacher}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import util.Util.rest

object StudentRouter {
  implicit val studentEncoder: EntityEncoder[IO, Student] = jsonEncoderOf[IO, Student]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]
  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val rateReq: EntityEncoder[IO, GradeReq] = jsonEncoderOf[IO, GradeReq]


  def studentRouter(studentService: StudentService): HttpRoutes[IO] = {
    HttpRoutes.of {

      case GET -> Root / "lessons" / LongVar(teacherId) => studentService.getLessonByTeacher(teacherId)
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "lesson" / LongVar(lessonId) => studentService.getLesson(lessonId)
        .forbidden

      case req@GET -> Root / "your-lesson" / LongVar(lessonId) => studentService.getYourLesson(req, lessonId)
        .forbidden

      case req@POST -> Root / "lesson" / "homework" => req.decodeJson[HomeworkReq]
        .flatMap( studentService.sendHomework(req, _)).forbidden

      case req@POST -> Root / "lesson" / LongVar(lessonId) / "sign-up" => studentService.signUp(req, lessonId)
        .forbidden

      case req@POST -> Root / "lesson" / LongVar(lessonId) / "sign-out" => studentService.signOut(req, lessonId)
        .flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "upcoming-lessons" => studentService.upcomingLessons(req)
        .flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "previous-lessons" => studentService.previousLessons(req)
        .flatMap(res => Ok(res.asJson))

      case req@GET -> Root / "next" => studentService.nextLesson(req)
        .flatMap(res => Ok(res.asJson))

      case req@POST -> Root / "evaluate-teacher" => req.decodeJson[GradeReq].flatMap(studentService.evaluateTeacher(req, _))
        .forbidden

      case req@POST -> Root / "cash-in" => req.decodeJson[CashInReq].flatMap(studentService.cashIn(req, _))
        .forbidden

    }
  }
}